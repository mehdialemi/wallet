# wallet
A simple wallet server and client communicating over gRpc


## Prerequisite
* Install the lastest gradle version
* Install docker community edition
* Install MySQL server and configure it (MySQL conf file is located at `etc/mysql/mysql.conf.d/mysqld.cnf`)
  - Accept all requests by setting bind-address 0.0.0.0
  - Increase max_connections for performance test


## Configuration
There configuration files are located at `src/main/resources`
* Hibernate (hibernate.cfg.xml)
* Logging (logback.xml)
* Wallet (wallet.properties)

### Hibernate Configuration (hibernate.cfg.xml)
* In `connection.url` change `localhost` to MySQL host
* Change `connection.username` to a valid MySQL username
* Change `connection.password` to a valid password corresponding for username
* Change `hbm2ddl.auto` from `create-drop` to `update` if you want to retain data between different wallet server runs
* Increase or Decrease `hibernate.hikari.maximumPoolSize` according to the MySQL configuration

### Loging (logback.xml)
Two appenders are defiend
* FILE
  - Write logs to the file address specified in `<fileNamePattern>log/wallet.%d{yyyy-MM-dd}.log</fileNamePatter>`
* STDOUT
  - Write logs to the Console

Currently, as denoted in `<root level="INFO">` **STDOUT** is active.
If you want to enable FILE, just uncomment `<appender-ref ref="FILE" />`

### Wallet Configuration (wallet.properties)
The configuration of wallet app (both client and server) could be defined in wallet.properties as key-values.
Here, we explain each property.
* `server.host = localhost`: the address of wallet server
* `server.port = 8080`: the listening port of wallet server on the running machine
  - Note: If you have changed this property, you should also update the `Dockerfile` in the root directory to expose docker to the new specified port
* `server.report.period.second = 5`: the report period in serve to log statistics (calls/second and durations) and in client to log progress. 
* `server.threads = 200`: number of grpc threads to accept requests
* `client.stream.enable = true`: enable async in client (to use different threads for sending and receiving messages)
* `client.response.store = false`: store responses in a queue in the client side. Enable this property only in the test classes.

## Run Server
After successfull setup and running the applications in the prequesite section, you can simply run the server by executing the `deploy-server.sh` script
To do this, you can find `deploy-server.sh` in the root directory of the Wallet project and run it as
`./deploy-server.sh`
This causes to
  - Build projects to the `./build` directory
  - Build docker file
  - Run a docker container and start Wallet Server listening on port 8080

**Note:** if you want to use another port, remember to change port 8080 both in the `./deploy-server.sh` and `wallet.properties` files.


## Run Client
To run client, execute the wallet-client script in `./build/install/wallet/bin/wallet-client`

For example you can run it as the following command
./build/install/wallet/bin/wallet-client -config src/main/resources/wallet.properties -u 50 -t 10 -r 100

In the above command, there are 4 cli parameters:
* `-config`: specify the wallet config file (default is pointing to the src/main/resources/wallet.properties)
* `-u`: number of wallet user (default is 1)
* `-t`: number of threads per user (default is 1)
* `-r`: number of performing rounds in each user thread (default is 1)
