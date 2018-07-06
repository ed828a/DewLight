# DewLight
Dewbe without DB
## Architecture
The app uses ViewModel to abstract the data from UI and MovieRepository as single source of truth for data. MovieRepository first fetch the data from database if exist than display data to the user and at the same time it also fetches data from the webservice and update the result in database and reflect the changes to UI from database.

![](https://github.com/ed828a/DewLight/blob/master/archtiture-net.png)
