package com.github.vuzoll.explorevk.domain.exploration

class Distribution<T> {

    int totalCount
    int nullCount
    SortedSet<DistributionEntry<T>> distributionEntries

    Distribution() {
        totalCount = 0
        nullCount = 0
        distributionEntries = new TreeSet<>()
    }

    void add(T object) {
        totalCount++
        if (object == null) {
            nullCount++
        } else {
            DistributionEntry distributionEntry = distributionEntries.find({ it.object == object })
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
