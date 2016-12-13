package com.github.vuzoll.explorevk.domain

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class VkProfile {

    Integer vkId
    String name
    City city
    Country country
    Set<EducationRecord> educationRecords
}
