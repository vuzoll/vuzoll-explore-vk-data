package com.github.vuzoll.explorevk.domain

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = 'vkId')
class Faculty {

    Integer vkId
    String name
    University university
}
