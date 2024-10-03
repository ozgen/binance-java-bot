
# Binance Java Bot

## Introduction

Coming soon...

## Features
- Coming soon...

## Getting Started

### Prerequisites
- Java 11
- Maven
- Binance account
- Telegram account

### Installation
1. Clone the repo: `git clone https://github.com/ozgen/binance-java-bot.git`
2. Navigate to the directory: `cd binance-java-bot`
3. Build with Maven: `mvn clean install`

### Configuration
Configure the application using environment variables or a `.env` file. Here are the key variables you need to set:

#### Bot Investment Configuration
- `app.bot.investment.currency`: Investment currency (e.g., `BTC`)
- `app.bot.investment.amount`: Total investment amount
- `app.bot.investment.perAmount`: Amount per trade
- `app.bot.investment.currencyRate`: Currency rate for conversion (e.g., `BTCUSD`)
- `app.bot.investment.percentageInc`: Percentage increase for buying
- `app.bot.investment.profitPercentage`: Profit percentage target

#### Bot Schedule Configuration
- `app.bot.schedule.buyError`: Schedule interval for buy error in ms
- `app.bot.schedule.sellError`: Schedule interval for sell error in ms
- `app.bot.schedule.insufficient`: Schedule interval for insufficient balance in ms
- `app.bot.schedule.notInRange`: Schedule interval for not in range status in ms
- `app.bot.schedule.tradingSignal`: Schedule interval for trading signals in ms
- `app.bot.schedule.openSellOrder`: Schedule interval for open sell orders in ms
- `app.bot.schedule.openBuyOrder`: Schedule interval for open buy orders in ms
- `app.bot.schedule.monthBefore`: Number of months before for date calculations

#### New Configuration for Telegram Error Reporting
- `bot.telegram.error.enabled`: Set to `true` to enable error reporting on Telegram.

### Running the Bot
Start the bot with:
```bash
java -jar target/binance-telegram-bot-0.0.1-SNAPSHOT.jar
```
## Setup Instructions

To ensure the system functions correctly, follow these setup requirements:

### Telegram Bot Setup
1. **Create a Telegram Bot**: Follow the instructions on [Telegram's official documentation](https://core.telegram.org/bots#creating-a-new-bot) to create a new bot.
2. **Generate a Bot Token**: Use this [guide](https://medium.com/geekculture/generate-telegram-token-for-bot-api-d26faf9bf064) to generate a token for your Telegram bot.


## Test Coverage

### Running Tests
Execute the test suite using Maven:
```bash
mvn test
```

### Coverage Reporting
After running the tests, generate a coverage report using JaCoCo (Java Code Coverage Library):
```bash
mvn jacoco:report
```
The coverage report can be found in `target/site/jacoco/index.html`. Open it in a web browser to view detailed coverage statistics.

## License
This project is licensed under the MIT License - see [LICENSE.md](LICENSE.md) for details.

## Acknowledgments
- Binance API
- Telegram API

---
