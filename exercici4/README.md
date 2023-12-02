# Epidemic Replication
## Exercise 4
In this system, the nodes that belong to the core layer have the most modern versions of the object. 
However, nodes belonging to the second layer have the oldest versions. 
Note that:
1. The different nodes of a same layer will always have the same version (maybe old, but the same!), 
2. By time = ∞ all nodes will converge towards the same version if the core layer does not propagate more updates (aka eventual consistency), 
3. The clients issue update transactions to the core layer, and 
4. Read-only transactions can be performed on any layer.

<br>
<br>

To run the program without having Go installed, you can use the Dockerfile provided in the repository:
```bash
docker build -t epidemic-replication .
docker run -p 8080:8080 epidemic-replication
```

If you have Go installed, just:
```bash
go run main.go
```
<br>

You can now access the web server at http://localhost:8080

<br>
<br>

To generate a new transactions.txt file, you can use the following python script:
```bash
cd client
python3 generate_transactions.py
```