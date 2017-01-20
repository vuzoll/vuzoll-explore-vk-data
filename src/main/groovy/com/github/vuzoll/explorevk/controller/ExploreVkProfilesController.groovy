package com.github.vuzoll.explorevk.controller

import com.github.vuzoll.explorevk.domain.vk.VkProfile
import com.github.vuzoll.explorevk.services.ExploreVkDatasetService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Slf4j
class ExploreVkProfilesController {

    @Autowired
    ExploreVkDatasetService exploreVkDatasetService

    @GetMapping(path = '/explore/profile/random')
    @ResponseBody VkProfile exploreRandomRecord() {
        log.info 'Receive random vk profile exploration request'

        exploreVkDatasetService.getRandomVkProfile()
    }

    @GetMapping(path = '/explore/profile/id/{id}')
    @ResponseBody VkProfile exploreRecordById(@PathVariable String id) {
        log.info "Receive exploration request for vk profile with id=${id}"

        exploreVkDatasetService.getVkProfileById(id)
    }

    @GetMapping(path = '/explore/profile/vkId/{vkId}')
    @ResponseBody VkProfile exploreRecordByVkId(@PathVariable Integer vkId) {
        log.info "Receive exploration request for vk profile with vkId=${vkId}"

        exploreVkDatasetService.getVkProfileByVkId(vkId)
    }
}
