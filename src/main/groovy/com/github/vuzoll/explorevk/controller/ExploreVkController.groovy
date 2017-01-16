package com.github.vuzoll.explorevk.controller

import com.github.vuzoll.explorevk.domain.vk.VkProfile
import com.github.vuzoll.explorevk.services.ExploreVkService
import com.github.vuzoll.explorevk.services.VkDatasetExploration
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@Slf4j
class ExploreVkController {

    @Autowired
    ExploreVkService exploreVkService

    @GetMapping(path = '/explore/dataset')
    @ResponseBody VkDatasetExploration exploreDataset() {
        log.info 'Receive dataset exploration request'

        exploreVkService.getVkDatasetExploration()
    }

    @GetMapping(path = '/explore/profile/random')
    @ResponseBody VkProfile exploreRandomRecord() {
        log.info 'Receive random vk profile exploration request'

        exploreVkService.getRandomVkProfile()
    }

    @GetMapping(path = '/explore/profile/id/{id}')
    @ResponseBody VkProfile exploreRecordById(@PathVariable String id) {
        log.info "Receive exploration request for vk profile with id=${id}"

        exploreVkService.getVkProfileById(id)
    }

    @GetMapping(path = '/explore/profile/vkId/{vkId}')
    @ResponseBody VkProfile exploreRecordByVkId(@PathVariable Integer vkId) {
        log.info "Receive exploration request for vk profile with vkId=${vkId}"

        exploreVkService.getVkProfileByVkId(vkId)
    }
}
