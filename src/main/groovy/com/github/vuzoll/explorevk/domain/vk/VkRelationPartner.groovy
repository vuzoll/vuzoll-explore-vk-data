package com.github.vuzoll.explorevk.domain.vk

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = 'vkId')
class VkRelationPartner {

    Integer vkId
    String firstName
    String lastName
}
