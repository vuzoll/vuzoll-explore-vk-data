package com.github.vuzoll.explorevk.domain.exploration

class Distribution<T> {

    int totalCount = 0
    int nullCount = 0
    SortedSet<DistributionEntry<T>> distributionEntries = new TreeSet<>({ o1, o2 -> (o2.count <=> o1.count) })

    void add(T object) {
        totalCount++
        if (object == null) {
            nullCount++
        } else {
            DistributionEntry distributionEntry = distributionEntries.get(object)
            if (distributionEntry == null) {
                distributionEntry = new DistributionEntry(object)
                distributionEntries.add(distributionEntry)
            }

            distributionEntry.inc()
        }
    }

    void add(Collection<T> objects) {
        objects.each(this.&add)
    }
}
