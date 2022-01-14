# PriceGetter
Get the price of different tokens on different chains with historical data

## Installation
The service can be compiled / dockerized with maven
```bash
mvn install
```
Docker deployment can be automated with ```mvn deploy``` (see the com.spotify.dockerfile-maven-plugin maven module)

The service should be connected to a database that indexed the ts and blockNumber of its blocks (see https://github.com/ChartsBot/blockhain_scrapper/tree/main/bch-blocks-indexer)

## Configuration
See application.base.conf (or application.docker.conf)

