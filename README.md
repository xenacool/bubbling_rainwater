(WARNING: Gradiose ambitious claims ahead.)

# what is this thing
Bubbling Rainwater is the database I want. It is a bring-your-own-bucket (BYOB) distributed structured query language (SQL) database that is tuned for statically and dynamically known transactional and analytical patterns. It prioritizes security, low-maintenance, evolvability, cost-efficiency, multi-tenancy, durability, consistency, availability, subscribability, and adaptivity to new hardware and network configuration.

# what is the purpose of the name?
Don't let the cloud own rainwater. A least-common-denominator of cloud computing is a commodity across many providers and self-hosted solutions. In the author's experience, high volume tabular and observability databases are the most fragile, expensive, and low-quality bottlenecks provided by the cloud today.

Bubbling refers to the constant motion of the data, in contrast to other projects involving data formats such as iceberg designed for long-term cold storage.

# big idea
Make an easy disaggregated HA cloud-native HTAP distributed SQL database that avoids single points of failure. It should be easy for administrators to ensure checkpointing, backup, and recovery using any s3-compatible object storage provider such as Minio, S3, Google Cloud Storage, CloudFlare, and OSS. Administrators manage the SQL database using evolvable abstractions such as partitions for spreading work, cold/warm/hot availability, and lenses. A chain is indexed by it's logical node identifier, contains physical nodes that can come and go, and owns partitions of data. Partitions are placed near their usage, the storage layer is made pluggable using a flexible redo logging abstraction inspired by Meta's Delos, a consistent hashing ring implementation is carefully considered, and an embedded consensus scheme provided by Apache Ratis determines ordering for chain replication to reduce contention and network chatter during epoch acquiescence, replication, and read/write transaction locking.

The contents of the dataplane buckets are Apache Paimon s3-compatible log-structured merge-trees for the recent data and optionally compacted columnar Apache iceberg files for older data. Every node can participate as in control plane and data plane depending on their age and random assignment. The control plane provides updates to the snapshot in the catalog whenever an epoch finishes.

# data model
account / catalog / namespace / table / partition

# security model
Integration with shiro to provide granular restrictions by catalog, namespace, table, column, and partition-key.

# plugin model
paimon/flink's plugin model looks good

# inspo
Project Cambria implements Edit Lenses (easiest link on this page to understand)
https://www.inkandswitch.com/cambria/

Edit Lenses (academic type theory of bidirectional transformations, not easy)
http://dmwit.com/papers/201107EL.pdf


High-throughput chain replication for read-mostly workloads
https://pdos.csail.mit.edu/6.824/papers/craq.pdf

WPaxos: Wide Area Network Flexible Consensus
https://arxiv.org/pdf/1703.08905

Calvin: Fast Distributed Transactions for Partitioned Database Systems
http://cs.yale.edu/homes/thomson/publications/calvin-sigmod12.pdf

High-Performance Transactions 6.5830/6.5831 Lecture 19 Tianyu Li
http://db.lcs.mit.edu/6.5830/lectures/lec19-2022.pdf

Virtual Consensus in Delos
https://research.facebook.com/publications/virtual-consensus-in-delos/

Fast Checkpoint Recovery Algorithms for Frequently Consistent Applications
https://www.cs.cornell.edu/~wmwhite/papers/2011-SIGMOD-Checkpoint.pdf

# unknowns
- paimon replication strategy
- control plane replication strategy

# architecture
- bubbling rainwater = s3-compatible-object storage + bubbling-control-plane + bubbling-dataplane
- bubbling-dataplane nodes implement a create-read-update-delete REDO logging subset of the Apache Flight protocol inspired by Meta's Delos paper
- bubbling-dataplane nodes durably replicate to s3 or cdc-chain-replicate
- bubbling-controlplane nodes schedule bubbling-executors dynamically near replicas in the bubbling dataplane.
- bubbling-controlplane nodes schedule pgwire endpoints for users toc onnect to.
- bubbling-executor instances provide adhoc analytical query support as an evictable concern of transactional writers
- every node is identical in what it can own, how important it's chains are
- apis
  - There is a stoppable GRPC service from Apache Ratis. This is used by BR for determining chain replication membership and ordering of nodes for chain replication. There is a monotonic counter for epoch.
  - Using this Ratis defined consensus-node-ordering information, Apache Arrow's Flight protocol will provide network primitives for Delos-inspired configurable chain-replication-with-apportioned-queries.
  - Create postgres connection. This returns a pgwire connection string for either admin, production-txn, or read-only-stale-analytical.
    - If no connections are open then all of the partitions and catalog entry will eventually-always (<>[]) only be in long-term cold storage, not warm disk, memory, or replicated.
    - An abstraction called "wide-chain-rings" provides APIs for finding replicas of partition, determining replica ordering, and finding other partitions. Chain replication, consistent hashing, and epoch-txns are done with awareness of this structure. src/quint-src has a specification for this.
    - Admin is used for initializing or joining a physical-node to a wide-chain-ring's logical-node by imputing object storage information, which also provides service-discovery information for Ratis.
      - The zeroth logical chain provides coordination for the full wide-chain-ring when necessary, such as modifications and incrementing the log.
    - Production-txn provides serializable and fast transactional SQL with caveats about the expressiveness of queries but bounds on the latency.
      - If production-txn partitions are warm then commit (durability) is after object-storage file commit success.
      - if production-txn partitions are hot then commit is after n=3 chain replication.
        - Availability-zone-independence is a configuration option for the dataplane and control planes independently which will force these replicas to be split or joined to availability-zones.
    - Read-only-analytical mode provides reporting and machine learning use-cases with tall calculations, aggregations, and insights. The results will be serializably consistent, but they may be behind production-txn connections.
    - The max epoch is held back to low watermark as determined by the presence of an entry for the global sum of operations at the start of epoch clock.
    - Applications should not combine data from these production-txn and read-only-analytical connections and should use each connection exclusively.
- some nodes are temporarily the heads of the chains based on an embedded consistency mechanism (paxos/rust with omnipaxos or raft/java with ratis)
- every chain has an epoch
- initializiation works like this
  - each host has K slots called nodes which are independent units of context.
  - each node has information about it's fault zone, including the host and any context passed to the host such as availability zone, data center, or power source.
  - catalog replication factor (options are 1,3,5,7,9) determines how may nodes are minimally required for writes to be committed.
  - committed-s3 means that a write will not be considered durable until it's written to s3.
  - namespace replication factor (options are 1,3,5,7) determines how many nodes are minimally required for writes to be committed.
  - if you want a hot control plane then you need 3,5,7 or 9 nodes
  - start N nodes with configuration for a s3-compatible bucket only they are given, ports, 
  - gossip node identities as uuids with start-timestamps and state-machine information into s3 buckets. use uuid node identity as subfolders. Find the N older nodes, using the uuids as tie-breakers for time-stamp comparisons.
    - WideChainRings are an embedded routing abstraction for wide-area consistent-hashing-ring over chain-replication
- the test strategy is TPC-C modified and tested by jepsen's Elle (?https://github.com/ydb-platform/tpcc-postgres.git https://github.com/jepsen-io/elle)

# tasks
- grpc for chain replication
- grpc for HashPartition to LogicalNode and LogicalNode to PhysicalNode
- grpc for a window'd udf callbacks for a workflow to implement what does this get me (https://datafusion.apache.org/library-user-guide/adding-udfs.html)
  - we provide grpc proto's that clients implement. these act as callbacks for window'd udfs

# basic design
- Initially there are only two operating modes that can be requested when a postgres connection is requested: transactional and analytical.
- Transactional requires that only few partitions can participate in a write or read, partition keys must be statically enumerable, and the connection is fresh. Analytical means that many transactions can participate in a read but the connection is stale.
- Transactions are epoch based.
- Views will be implemented later using a DAG with Chandy-Lamport checkpointing. If the CL checkpoint barrier is included in the transaction epoch then the checkpoint barrier is transactionally consistent.
- Partitions are placed predictively near readers using optional hints.
  - Partitions can be hot (replicated at n=3,5,7,9), cold (replicated at n=0), or warm (replicated at n=1).
- The replication scheme can be extended to provide read-replication on the fly.
  - The Meta Delos paper provides an elegant logging API that's recycled here.
- Minimal high-availability implies that the control plane partitions are always hot. More highly available than this implies that the data plane partitions are hot.
  - This allows us to add Chandy-Lamport Views later.
- configurable redo-logging abstraction from Delos used for control plane and data plane
- Initially, java Apache paimon over grpc will provide the current transactional write path for every partition for every control plane and data plane store. Paimon is a log-structured merge-tree (LSM) with bloom filters that produces append-only files so that it can write to s3 without needing coordination.
- A control plane will be written which provides an initial multi-tenant catalog with grpc administrator and control plane APIs that can create administrators, users, catalogs, and namespaces.
- For optional recommended high-availability, namespaces can be configured to replicate Paimon CDC. Paimon CDC combined with the latest snapshot s3 is necessary and sufficient to recover from failure, the snapshot isn't necessary to be on the replicas during replication.
- Namespaces are collections of tables that can be written to with serializable consistency and analytically queries for snapshot reads.
- The control plane can be used to request a (stale, adhoc) analytical or (fresh, writable) transactional connection. Internally this will initialize a pgwire session and notify relevant namespaces that a reader is online.

version vector
async table sequencer that uses the consistent hashing ring/chain replication virtual node layer to lock rows until the next (chain-replicated) chandy-lamport checkpoint or snapshot
object storage service discovery for raits (or omnipaxos) to set-up a multi-tenant secure available catalog service
Test serializable isolation using jepsen/elle.

# what does this get me?
- Extremely low operational burden for a reasonable, portable, distributed SQL database.
- High volume time-series analysis can be a valuable solution for problems such as predictive maintenance, controls monitoring, capacity planning, and optimization. These use-cases require efficient, data-intensive infrastructure with hybrid transactional analytical access.
- These use-cases are chosen because they are difficult to achieve efficiently with existing databases.
- High assurances at little overhead in modern computing environments can be achieved cost-effectively by relying on object storage for hot and cold data with compatible data connectors.

## Tenets
Provide safety guarantees but consider the common reality.
- This matters with network chatter and data analysis.
- In common networks we should optimize for infrequency of data partitions instead of constantly voting.
- In common data analysis we're making multiple comparisons based on finite sample sizes, but our models preposterously assume single decisions over infinite trials.

## Why did you use Java and Rust?


## License

This library is released under Apache license.
