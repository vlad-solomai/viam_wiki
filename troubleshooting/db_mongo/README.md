# Mongo DB

### Creating and Assigning the User
The first method will create the user and assign it the read permissions that he needs. In this case read only access to the mytest db.
First logon to mongodb and switch to the admin database:
```
$ mongo -u dbadmin -p --authenticationDatabase admin
> use admin
switched to db admin
```
List the dbs:
```
> show dbs
admin       0.000GB
mytest      0.000GB
```
List the collections and read the data from it for demonstration purposes:
```
> use mytest
> show collections;
col1
col2
> db.col1.find()
{ "_id" : ObjectId("5be3d377b54849bb738e3b6b"), "name" : "ruan" }
> db.col2.find()
{ "_id" : ObjectId("5be3d383b54849bb738e3b6c"), "name" : "stefan" }
```
Now create the user collectionreader that will have access to read all the collections from the database:
```
$ > db.createUser({user: "collectionreader", pwd: "secretpass", roles: [{role: "read", db: "mytest"}]})
Successfully added user: {
  "user" : "collectionreader",
  "roles" : [
    {
      "role" : "read",
      "db" : "mytest"
    }
  ]
}

$ > db.getUsers();
[
  {
    "_id" : "mytest.collectionreader",
    "user" : "collectionreader",
    "db" : "mytest",
    "roles" : [
      {
        "role" : "read",
        "db" : "mytest"
      }
    ]
  }
]
```
Exit and log out and log in with the new user to test the permissions:
```
$ mongo -u collectionreader -p --authenticationDatabase mytest
> use mytest
switched to db mytest
> show collections
col1
col2

> db.col1.find()
{ "_id" : ObjectId("5be3d377b54849bb738e3b6b"), "name" : "ruan" }
```
Now let's try to write to a collection:
```
> db.col1.insert({"name": "james"})
WriteResult({
  "writeError" : {
    "code" : 13,
    "errmsg" : "not authorized on mytest to execute command { insert: \"col1\", documents: [ { _id: ObjectId('5be3d6c0492818b2c966d61a'), name: \"james\" } ], ordered: true }"
  }
})
```
It works as expected.

### Create Roles and Assign Users to the Roles
In the second method, we will create the roles then assign the users to the roles. And in this scenario, we will only grant a user reader access to one collection on a database. 
Login with the admin user:
```
$ mongo -u dbadmin -p --authenticationDatabase admin
> use admin
```
First create the read only role myReadOnlyRole:
```
> db.createRole({ role: "myReadOnlyRole", privileges: [{ resource: { db: "mytest", collection: "col2"}, actions: ["find"]}], roles: []})
```
Now create the user and assign it to the role:
```
> db.createUser({ user: "reader", pwd: "secretpass", roles: [{ role: "myReadOnlyRole", db: "mytest"}]})
```
Similarly, if we had an existing user that we also would like to add to that role, we can do that by doing this:
```
> db.grantRolesToUser("anotheruser", [ { role: "myReadOnlyRole", db: "mytest" } ])
```
Logout and login with the reader user:
```
$ mongo -u reader -p --authenticationDatabase mytest
> use mytest
```
Now try to list the collections:
```
> show collections
2018-11-08T07:42:39.907+0100 E QUERY    [thread1] Error: listCollections failed: {
  "ok" : 0,
  "errmsg" : "not authorized on mytest to execute command { listCollections: 1.0, filter: {} }",
  "code" : 13,
  "codeName" : "Unauthorized"
}
```
As we only have read (find) access on col2, lets try to read data from collection col1:
```
> db.col1.find()
Error: error: {
  "ok" : 0,
  "errmsg" : "not authorized on mytest to execute command { find: \"col1\", filter: {} }",
  "code" : 13,
  "codeName" : "Unauthorized"
}
```
And finally try to read data from the collection we are allowed to read from:
```
> db.col2.find()
{ "_id" : ObjectId("5be3d383b54849bb738e3b6c"), "name" : "stefan" }
```
And also making sure we cant write to that collection:
```
> db.col2.insert({"name": "frank"})
WriteResult({
  "writeError" : {
    "code" : 13,
    "errmsg" : "not authorized on mytest to execute command { insert: \"col2\", documents: [ { _id: ObjectId('5be3db1530a86d900c361465'), name: \"frank\" } ], ordered: true }"
  }
})
```

### Assigning Permissions to Roles
If you later on want to add more permissions to the role, this can easily be done by using grantPrivilegesToRole():
```
$ mongo -u dbadmin -p --authenticationDatabase admin
> use mytest
> db.grantPrivilegesToRole("myReadOnlyRole", [{ resource: { db : "mytest", collection : "col1"}, actions : ["find"] }])
```
To view the permissions for that role:
```
> db.getRole("myReadOnlyRole", { showPrivileges : true })
```

### Updates a user's password
Run the method in the database where the user is defined:
```
use products
db.changeUserPassword("accountUser", passwordPrompt())
db.changeUserPassword("accountUser", "SOh3TbYhx8ypJPxmt1oOfL")
```
### How to configure replication
Only on Primary:
```
rs.initiate()
rs.status()
rs.add( { host: "wdb02-env:27017", priority: 0, votes: 0 } )
rs.add( { host: "wdb03-env:27017", priority: 0, votes: 0 } )
rs.conf()
```
After that we need to add mongo indexes on the primary node:
```
sudo mongo --username admin --password password --authenticationDatabase admin
use database;
db.createUser({    user: "gameiom", pwd: "password", roles:[{role: "readWrite" , db:"database"}]})


db.createCollection("stateJournal", { capped : false} )
db.stateJournal.createIndex({"txKey" : 1.0 })
db.stateJournal.createIndex({"datetime" : 1.0 })
db.stateJournal.createIndex({"sessionId" : 1.0,"gameId" : 1.0, "providerId" : 1.0,"username" : 1.0, "operatorId" : 1.0, "mode" : 1.0})


db.stateJournal.createIndex({"gameId" : 1.0, "providerId" : 1.0, "username" : 1.0, "operatorId" : 1.0, "mode" : 1.0})
```

### Management
1. Connect to one of the instances in the replica set, and do the following mongodb commands to save the current replica set configuration in a variable. The print json function will print the config data of the current replica set configuration.
```
config = rs.conf();
printjson(config);
```
2. Modify the members fields to remove one of the instances. Assume you have 3 instances in the config.members and you want to remove the first one. The force true option forces the current instance to use the new configuration and it will be propagated to all other instances listed in the config.members array.
```
config.members = [config.members[1], config.members[2]];
rs.reconfig(config, {force: true});
```
3. If there is an instance that is not working due to hardware failure or network connections or whatever reasons, and you want to remove that from the replica set.
```
config = rs.config();
rs.reconfig(config, {force: true});


OR

cfg = rs.conf()
cfg.members[0].host = "mongo2.example.net"
rs.reconfig(cfg)
```
4. Remove an instance by specifying the instance endpoint using the rs.remove(), this command will remove the instance located in example.com:27017
```
rs.remove("example.com:27017");
```
5. Add an instance
```
rs.add("example.com:27017");
```
