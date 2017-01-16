package com.github.vuzoll.explorevk.domain.vk

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = 'vkId')
class VkRelative {

    Integer vkId
    String type
}
