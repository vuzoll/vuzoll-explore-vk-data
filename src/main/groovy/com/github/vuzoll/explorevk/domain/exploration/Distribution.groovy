package com.github.vuzoll.explorevk.domain.exploration

import org.springframework.data.annotation.Transient

class Distribution<T> {

    @Transient
    private final Closure<Boolean> isNullPredicate

    @Transient
    private final Map<T, DistributionEntry<T>> distributionEntries

    int numberOfOptionsLimit

    int totalCount
    int nullCount
    int notNullCount
    int numberOfOptions
    SortedSet<DistributionEntry<T>> distribution

    Distribution() {
        this({ it == null }, Integer.MAX_VALUE)
    }

    Distribution(int numberOfOptionsLimit) {
        this({ it == null }, numberOfOptionsLimit)
    }

    Distribution(Closure<Boolean> isNullPredicate, int numberOfOptionsLimit) {
        this.isNullPredicate = isNullPredicate
        this.numberOfOptionsLimit = numberOfOptionsLimit

        this.totalCount = 0
        this.nullCount = 0
        this.notNullCount = 0
        this.numberOfOptions = 0
        this.distributionEntries = [:]
        this.distribution = new TreeSet<>()
    }

    void add(T object) {
        totalCount++
        if (isNullPredicate.call(object)) {
            nullCount++
        } else {
            notNullCount++
            DistributionEntry distributionEntry = distributionEntries.getOrDefault(object, new DistributionEntry(object))

            numberOfOptions = distributionEntries.size()
            distribution = distributionEntry.collect({ key, value -> value }).sort().take(numberOfOptionsLimit)
            distributionEntry.count++
            distributionEntry.percentageTotal = distributionEntry.count / totalCount
            distributionEntry.percentageNotNull = distributionEntry.count / notNullCount
        }
    }

    void add(Collection<T> objects) {
        objects.each(this.&add)
    }
}
