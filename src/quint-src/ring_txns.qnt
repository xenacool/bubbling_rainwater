// This module models data consistency of a multi-object transactions over an epoch-based
// consistent hashing scheme. The transaction batch should be able to partially succeed
// even if a node is down. In practice each of the participants of the ring are made highly
// and durable but they may experience transient outages which should not interrupt the ring.

// The purpose of this design is for geo-replicated databases.
// TODO:
//   - Add node failure
//   - Apportioned queries.
//   - R/W locking for txns.
//   - txn batching occurs by round-tripping the partitions in the consistent hashing ring,
//     incrementing a transaction_epoch when the partitions wrap around from N to 0.

module ring_txns {
  // how many operations have happened?
  type LogicalClock = int
  // what partition is this?
  type Partition = int
  // how many operations have happened in this partition?
  type PartitionClock = LogicalClock
  // how many transaction batches have acquiesced?
  type EpochClock = LogicalClock
  // how many operations occurred in this partition in every transaction batch?
  type EpochPartitionLog = EpochClock -> PartitionClock
  // how many operations occurred in every partition?
  type SumPartitionClock = PartitionClock
  // how many operations occurred in every partition before the start of this partition's txn batch
  type EpochSumPartitionLog = EpochClock -> SumPartitionClock

  type RingParticipant = {
    // is this functional
    functional: bool,
    // what partition is this?
    partition: Partition,

    // single-object operations available to read
    committed_partition_clock: PartitionClock,
    // in-flight writes
    uncommitted_partition_clock: PartitionClock,

    // transaction-batch operations available to read
    committed_epoch_clock: EpochClock,
    // how many operations occurred in this partition at the start of every transaction batch
    epoch_partition_log: EpochPartitionLog,
    // how many operations occurred in every partition at the start of every epoch for me 
    // if this participant is the Nth partition then the epoch sum will be all
    // operations from previous epoch and the current epoch up to partition 0..N-1
    epoch_sum_participation_log: EpochSumPartitionLog,
  }

  type OptionalParticipant = None | Some(RingParticipant)

  pure def new_ring_participant(partition) = {
    {
      functional: true,
      partition: partition,

      committed_partition_clock: 0,
      uncommitted_partition_clock: 0,

      committed_epoch_clock: 0,
      epoch_partition_log: Map(0 -> 0),
      epoch_sum_participation_log: Map(0 -> 0)
    }
  }

  var partitions: Set[Partition]
  var ring_participants: Partition -> RingParticipant

  action initialize_partitions() = {
    partitions' = Set(0,1,2)
  }

  action initialize_ring() = {
    ring_participants' = Set(0,1,2)
      .mapBy(elem => new_ring_participant(elem))
  }

  action init = {
    all {
      initialize_partitions,
      initialize_ring,
    }
  }

  action fail_participant = {
    nondet partition = oneOf(partitions)
    //if (ring_participants.get(partition).functional
    // and ring_participants.get(partition).filter(e => (e.functional and e.partition != partition).size() > ring_participants.size() / 2))
     // ring_participants.set(partition, ring_participants.get(partition).with("functional", false))
    //else
    //  ring_participants.set(partition, ring_participants.get(partition))    
  }
  // p = Partition
  // i = EpochClock

  // epoch_partition_log = new EpochPartitionLog()
  // epoch_partition_log[p][0] = 0 -> k

  // epoch_sum_log = new EpochSumPartitionLog()
  // epoch_sum_log[0][0] = 0 -> 0
  // epoch_sum_log[p][i] = epoch_sum_log[p-1][i] + epoch_partition_log[p-1][i] - epoch_partition_log[p-1][i-1]

  pure def max(x,y) = {
    if (x > y)
      x
    else
      y
  }
  
  pure def min(x,y) = {
    if (x > y)
      y
    else
      x
  }

  pure def successors(servers, target) = {
    servers.foldl([], (init, elem) => if (elem > target)
      init.append(elem)
    else
      init)
  }

  pure def predecessors(servers, target) = {
    servers.foldl([], (init, elem) => if (elem < target)
      init.append(elem)
    else
      init)
  }

  action step = {
    any {
      fail_participant
    }
  }

  // same epoch implies same committed values
  val invariant_ring_agrees: bool = { 
    true
  }

}