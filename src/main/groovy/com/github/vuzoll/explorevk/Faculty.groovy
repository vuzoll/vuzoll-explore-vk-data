package com.github.vuzoll.explorevk

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = 'vkId')
class Faculty {

    Integer vkId
    String name
    University university
}
