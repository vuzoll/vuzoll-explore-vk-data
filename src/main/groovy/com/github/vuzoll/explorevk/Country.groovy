package com.github.vuzoll.explorevk

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = 'vkId')
class Country {

    Integer vkId
    String name
}
