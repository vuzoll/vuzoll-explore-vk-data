package com.github.vuzoll.explorevk

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = 'vkId')
class University {

    Integer vkId
    String name
}
