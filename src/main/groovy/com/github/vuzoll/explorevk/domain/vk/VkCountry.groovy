package com.github.vuzoll.explorevk.domain.vk

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = 'vkId')
class VkCountry {

    Integer vkId
    String name
}
