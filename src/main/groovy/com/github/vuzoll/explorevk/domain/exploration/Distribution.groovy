package com.github.vuzoll.explorevk.domain.exploration

class Distribution<T> {

    int totalCount = 0
    int nullCount = 0
    SortedSet<DistributionEntry<T>> distributionEntries = []

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
