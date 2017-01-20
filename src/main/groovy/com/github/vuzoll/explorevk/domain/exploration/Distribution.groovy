package com.github.vuzoll.explorevk.domain.exploration

import org.springframework.data.annotation.Transient

class Distribution<T> {

    @Transient
    private final Closure<Boolean> isNullPredicate

    @Transient
    private final Map<T, DistributionEntry<T>> distributionEntries

    Integer numberOfOptionsLimit

    int totalCount
    int nullCount
    int notNullCount
    int numberOfOptions
    SortedSet<DistributionEntry<T>> distribution

    Distribution() {
        this({ it == null }, null)
    }

    Distribution(Integer numberOfOptionsLimit) {
        this({ it == null }, numberOfOptionsLimit)
    }

    Distribution(Closure<Boolean> isNullPredicate, Integer numberOfOptionsLimit) {
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
            DistributionEntry distributionEntry = distributionEntries.get(object)
            if (distributionEntry == null) {
                distributionEntry = new DistributionEntry(object)
                distributionEntries.put(object, distributionEntry)
            }

            numberOfOptions = distributionEntries.size()
            distributionEntry.count++

            distribution = new TreeSet<>(distributionEntries.collect({ key, value -> value }))
            if (numberOfOptionsLimit != null) {
                distribution = distribution.take(numberOfOptionsLimit)
            }

            distribution.each {
                it.percentageTotal = it.count / totalCount
                it.percentageNotNull = it.count / notNullCount
            }
        }
    }

    void add(Collection<T> objects) {
        objects.each(this.&add)
    }
}
