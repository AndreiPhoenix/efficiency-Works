#!/bin/bash

echo "================================================"
echo "GC Log Analysis Tool"
echo "================================================"

if [ $# -eq 0 ]; then
    echo "Usage: $0 <gc-log-file> [output-format]"
    echo "Formats: text, json, html"
    exit 1
fi

GC_LOG="$1"
FORMAT="${2:-text}"
OUTPUT_FILE="gc-analysis-$(date +%Y%m%d-%H%M%S)"

if [ ! -f "$GC_LOG" ]; then
    echo "Error: GC log file not found: $GC_LOG"
    exit 1
fi

echo "Analyzing GC log: $GC_LOG"
echo "Format: $FORMAT"
echo ""

# Основные статистики
echo "=== BASIC GC STATISTICS ==="
TOTAL_GC=$(grep -c "GC pause" "$GC_LOG" 2>/dev/null || grep -c "\[GC" "$GC_LOG")
FULL_GC=$(grep -c "Full GC" "$GC_LOG" 2>/dev/null || grep -c "\[Full GC" "$GC_LOG")
YOUNG_GC=$((TOTAL_GC - FULL_GC))

echo "Total GC Events: $TOTAL_GC"
echo "Young GC Events: $YOUNG_GC"
echo "Full GC Events: $FULL_GC"

# Время GC
if grep -q "real=" "$GC_LOG"; then
    # G1GC формат
    TOTAL_TIME=$(grep "real=" "$GC_LOG" | awk -F'real=' '{print $2}' | awk '{print $1}' | sed 's/s//' | awk '{sum += $1} END {printf "%.2f", sum*1000}')
elif grep -q "Times:" "$GC_LOG"; then
    # ParallelGC формат
    TOTAL_TIME=$(grep "Times:" "$GC_LOG" | awk -F'Total time for which application threads were stopped:' '{print $2}' | awk '{print $1}' | sed 's/,//' | awk '{sum += $1} END {printf "%.2f", sum}')
else
    TOTAL_TIME="N/A"
fi

echo "Total GC Time: ${TOTAL_TIME}ms"

if [ "$TOTAL_GC" -gt 0 ] && [ "$TOTAL_TIME" != "N/A" ]; then
    AVG_TIME=$(echo "scale=2; $TOTAL_TIME / $TOTAL_GC" | bc)
    echo "Average GC Time: ${AVG_TIME}ms"
fi

echo ""

# Анализ пауз
echo "=== GC PAUSE ANALYSIS ==="
echo "Top 10 longest GC pauses:"

if grep -q "real=" "$GC_LOG"; then
    # G1GC
    grep "real=" "$GC_LOG" | awk -F'real=' '{print $2}' | awk '{print $1}' | sed 's/s//' | sort -nr | head -10 | while read time; do
        echo "  ${time}s"
    done
elif grep -q "\[GC pause" "$GC_LOG"; then
    # CMS/G1
    grep "\[GC pause" "$GC_LOG" | awk -F'->' '{print $2}' | awk '{print $1}' | sed 's/K//' | sort -nr | head -10 | while read kb; do
        mb=$(echo "scale=2; $kb / 1024" | bc)
        echo "  ${mb}MB cleaned"
    done
fi

echo ""

# Использование heap
echo "=== HEAP USAGE PATTERNS ==="
echo "Heap usage before/after GC:"

if grep -q "\[PSYoungGen:" "$GC_LOG"; then
    # Parallel Scavenge
    grep -A1 "\[Full GC" "$GC_LOG" | head -20 | while read line; do
        if echo "$line" | grep -q "\[PSYoungGen:"; then
            echo "  $line"
        fi
    done
elif grep -q "\[Eden:" "$GC_LOG"; then
    # CMS
    grep -B1 "\[CMS:" "$GC_LOG" | head -20
fi

echo ""

# Причины GC
echo "=== GC TRIGGERS ==="
echo "GC Trigger Reasons:"

if grep -q "Allocation Failure" "$GC_LOG"; then
    ALLOC_FAIL=$(grep -c "Allocation Failure" "$GC_LOG")
    echo "  Allocation Failure: $ALLOC_FAIL times"
fi

if grep -q "Metadata GC Threshold" "$GC_LOG"; then
    META_GC=$(grep -c "Metadata GC Threshold" "$GC_LOG")
    echo "  Metadata GC Threshold: $META_GC times"
fi

if grep -q "System.gc()" "$GC_LOG"; then
    SYS_GC=$(grep -c "System.gc()" "$GC_LOG")
    echo "  System.gc() calls: $SYS_GC times"
fi

echo ""

# Рекомендации
echo "=== RECOMMENDATIONS ==="

if [ "$FULL_GC" -gt 5 ]; then
    echo "⚠️  HIGH PRIORITY: Too many Full GC events ($FULL_GC)"
    echo "   - Increase heap size"
    echo "   - Review memory leaks"
    echo "   - Check for System.gc() calls"
fi

if [ "$TOTAL_GC" -gt 100 ]; then
    echo "⚠️  HIGH GC Frequency ($TOTAL_GC events)"
    echo "   - Increase young generation size"
    echo "   - Reduce allocation rate"
    echo "   - Consider object pooling"
fi

if [ "$TOTAL_TIME" != "N/A" ] && [ $(echo "$TOTAL_TIME > 5000" | bc 2>/dev/null || echo 0) -eq 1 ]; then
    echo "⚠️  HIGH GC Time (>5 seconds total)"
    echo "   - Tune GC parameters"
    echo "   - Reduce heap size if possible"
    echo "   - Consider different GC algorithm"
fi

YOUNG_RATIO=0
if [ "$TOTAL_GC" -gt 0 ]; then
    YOUNG_RATIO=$(echo "scale=2; $YOUNG_GC * 100 / $TOTAL_GC" | bc)
fi

if [ $(echo "$YOUNG_RATIO < 80" | bc 2>/dev/null || echo 0) -eq 1 ]; then
    echo "⚠️  LOW Young GC Ratio ($YOUNG_RATIO%)"
    echo "   - Objects promoted too quickly"
    echo "   - Increase -XX:MaxTenuringThreshold"
    echo "   - Review object lifetimes"
fi

echo ""
echo "=== SUGGESTED JVM OPTIONS ==="
echo "Based on the analysis:"
echo "-XX:+UseG1GC"
echo "-Xmx4g -Xms4g"
echo "-XX:MaxGCPauseMillis=200"
echo "-XX:ParallelGCThreads=4"
echo "-XX:ConcGCThreads=2"
echo "-XX:+UseStringDeduplication"

# Генерация отчета в выбранном формате
case $FORMAT in
    "json")
        cat > "${OUTPUT_FILE}.json" << EOF
{
  "analysis": {
    "timestamp": "$(date)",
    "gc_log": "$GC_LOG",
    "statistics": {
      "total_gc_events": $TOTAL_GC,
      "young_gc_events": $YOUNG_GC,
      "full_gc_events": $FULL_GC,
      "total_gc_time_ms": "$TOTAL_TIME",
      "average_gc_time_ms": "$AVG_TIME"
    },
    "recommendations": [
      $(if [ "$FULL_GC" -gt 5 ]; then echo "\"Too many Full GC events\","; fi)
      $(if [ "$TOTAL_GC" -gt 100 ]; then echo "\"High GC frequency\","; fi)
      "\"Review JVM options\""
    ]
  }
}
EOF
        echo "JSON report saved to: ${OUTPUT_FILE}.json"
        ;;

    "html")
        cat > "${OUTPUT_FILE}.html" << EOF
<!DOCTYPE html>
<html>
<head>
    <title>GC Analysis Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        .section { margin-bottom: 30px; }
        .warning { color: #d9534f; font-weight: bold; }
        .metric { background-color: #f5f5f5; padding: 10px; margin: 5px 0; }
        .recommendation { background-color: #dff0d8; padding: 10px; margin: 5px 0; }
    </style>
</head>
<body>
    <h1>GC Analysis Report</h1>
    <p>Generated: $(date)</p>
    <p>GC Log: $GC_LOG</p>

    <div class="section">
        <h2>Statistics</h2>
        <div class="metric">Total GC Events: $TOTAL_GC</div>
        <div class="metric">Young GC Events: $YOUNG_GC</div>
        <div class="metric">Full GC Events: $FULL_GC</div>
        <div class="metric">Total GC Time: ${TOTAL_TIME}ms</div>
    </div>

    <div class="section">
        <h2>Recommendations</h2>
        $(if [ "$FULL_GC" -gt 5 ]; then echo '<div class="warning">⚠️ Reduce Full GC events</div>'; fi)
        $(if [ "$TOTAL_GC" -gt 100 ]; then echo '<div class="warning">⚠️ Reduce GC frequency</div>'; fi)
        <div class="recommendation">Suggested JVM options:</div>
        <pre>-XX:+UseG1GC
-Xmx4g -Xms4g
-XX:MaxGCPauseMillis=200</pre>
    </div>
</body>
</html>
EOF
        echo "HTML report saved to: ${OUTPUT_FILE}.html"
        ;;

    *)
        # Text формат (по умолчанию)
        echo "Text report saved to: ${OUTPUT_FILE}.txt"
        cp /dev/null "${OUTPUT_FILE}.txt"
        {
            echo "GC Analysis Report"
            echo "=================="
            echo "Generated: $(date)"
            echo "GC Log: $GC_LOG"
            echo ""
            echo "Statistics:"
            echo "- Total GC Events: $TOTAL_GC"
            echo "- Young GC Events: $YOUNG_GC"
            echo "- Full GC Events: $FULL_GC"
            echo "- Total GC Time: ${TOTAL_TIME}ms"
            echo "- Average GC Time: ${AVG_TIME}ms"
            echo ""
            echo "Recommendations:"
            if [ "$FULL_GC" -gt 5 ]; then echo "- Reduce Full GC events"; fi
            if [ "$TOTAL_GC" -gt 100 ]; then echo "- Reduce GC frequency"; fi
            echo "- Review JVM options"
            echo ""
            echo "Suggested JVM Options:"
            echo "-XX:+UseG1GC"
            echo "-Xmx4g -Xms4g"
            echo "-XX:MaxGCPauseMillis=200"
        } > "${OUTPUT_FILE}.txt"
        ;;
esac

echo ""
echo "================================================"
echo "Analysis complete!"
echo "================================================"