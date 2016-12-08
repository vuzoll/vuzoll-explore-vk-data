package com.github.vuzoll.explorevk

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = 'vkId')
class City {

    Integer vkId
    String name
    Country country
}
