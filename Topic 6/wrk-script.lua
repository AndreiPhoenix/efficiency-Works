-- Скрипт для нагрузочного тестирования разных эндпоинтов
math.randomseed(os.time())

function setup(thread)
    thread:set("id", math.random(1, 15))
end

function init(args)
    requests = 0
    responses = 0
    urls = {
        "/api/products/1",
        "/api/products/2",
        "/api/products/3",
        "/api/products/all",
        "/api/products/category/Electronics",
        "/api/products/search?keyword=laptop"
    }
end

function request()
    requests = requests + 1

    -- Чередуем запросы: 70% кэшируемых, 30% создания/обновления
    if requests % 10 < 7 then
        -- GET запросы (из кэша)
        local url = urls[math.random(#urls)]
        return wrk.format("GET", url)
    else
        -- POST/PUT запросы (инвалидация кэша)
        if math.random() > 0.5 then
            local id = math.random(1, 15)
            local body = string.format('{"name":"Updated Product %d","price":%d}', id, math.random(100, 1000))
            return wrk.format("PUT", "/api/products/" .. id, wrk.headers, body)
        else
            local body = string.format('{"name":"New Product %d","category":"Test","price":%d}', requests, math.random(50, 500))
            return wrk.format("POST", "/api/products", wrk.headers, body)
        end
    end
end

function response(status, headers, body)
    responses = responses + 1
end