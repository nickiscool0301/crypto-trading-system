-- Initialize wallet with 50,000 USDT balance
INSERT INTO wallet (id, usdt_balance, eth_balance, btc_balance, updated_at)
VALUES (1, 50000.00000000, 0.00000000, 0.00000000, CURRENT_TIMESTAMP);
