# Overview

Simple trading system:
- Track crypto prices from Binance and Huobi 
- Let user trade with a virtual wallet


# Project Structure

```
src/main/java/com/trading/system/
├── domain/              # Business logic, repositories, models, entites, exception
├── application/         # Services, APIs, Exception handlers
└── infrastructure/      # Binance/Huobi clients, config, schedulers)
```

# API Endpoints

### Get Latest Prices
```bash
GET /api/prices/latest
```
Returns the current best prices for ETH and BTC (max for sell, min for buy)

### Get Wallet Balance
```bash
GET /api/wallet
```
Initial wallet: 50,000 USDT, 0 ETH, 0 BTC


### Execute a Trade
```bash
POST /api/trades
Content-Type: application/json

{
  "symbol": "ETHUSDT",
  "orderAction": "BUY",
  "quantity": 1.0
}
```

#### Notes
- `orderAction` **MUST** be `BUY` or `SELL`
- `symbol` **MUST** be `ETHUSDT` or `BTCUSDT`
- `quantity` **MUST** be a positive number

### View Trade History
```bash
GET /api/trades
```
Lists all your past trades.

# Configuration

Edit `src/main/resources/application.yaml` to change settings:

```yaml
trading:
  price:
    max-age-seconds: 30
```

## Notes
- Binance or Houbi can be possibly down (AWS Outage, Cloudfare outage, etc). In that case, we should not let users trade with the old best prices.
- We define the `max-ages` for each prices (default is 30s).
- If the prices from DB is over 30s, we will deny the API request to trade from user.

# Important note
## Rate Limiter
- Fetch data from Binance and Huobi is rate limited. It could potentially block the IP when we make too many requests.
- Implement Rate Limiter using Resilience4j to avoid that. Currently, the configuration is 90 requests per 10 seconds.
## Race Condition
- We only have 1 Wallet, so we need to make sure there is no race condition.
- Use Optimistic Locking to avoid race condition.

## Exception Handling
- Implement global exception handler to handle exceptions.


# Potential enhancemant
- API versioning
- Authentication/Authorization
- More test cases

