package com.github.vuzoll.explorevk.domain

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = 'vkId')
class Country {

    Integer vkId
    String name
}
