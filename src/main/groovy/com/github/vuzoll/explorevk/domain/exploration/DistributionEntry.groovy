package com.github.vuzoll.explorevk.domain.exploration

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = 'object')
class DistributionEntry<T> implements Comparable<DistributionEntry<T>> {

    T object
    int count

    DistributionEntry(T object) {
        this.object = object
        this.count = 0
    }

    void inc() {
        count++
    }

    @Override
    int compareTo(DistributionEntry<T> o) {
        o.count <=> this.count
    }
}
