package com.github.vuzoll.explorevk.controller

import com.github.vuzoll.explorevk.domain.vk.VkProfile
import com.github.vuzoll.explorevk.services.ExplorationService
import com.github.vuzoll.explorevk.services.ExploreVkDatasetService
import com.github.vuzoll.explorevk.domain.exploration.VkDatasetExploration
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@Slf4j
class ExploreVkController {

    @Autowired
    ExplorationService explorationService

    @Autowired
    ExploreVkDatasetService exploreVkDatasetService

    @PostMapping(path = '/explore/dataset')
    @ResponseBody VkDatasetExploration exploreDataset() {
        log.info 'Receive dataset exploration request'

        VkDatasetExploration currentlyRunningExploration = explorationService.getCurrentlyRunningExploration()
        if (currentlyRunningExploration != null) {
            log.error "Service is busy with another exploration id=$currentlyRunningExploration.id, can't accept new one"
            throw new IllegalStateException("Service is busy with another exploration id=$currentlyRunningExploration.id, can't accept new one")
        }

        return explorationService.startNewExploration()
    }

    @GetMapping(path = '/exploration/{explorationId}')
    @ResponseBody VkDatasetExploration explorationStatus(@PathVariable String explorationId) {
        explorationService.explorationStatus(explorationId)
    }

    @GetMapping(path = '/exploration/last')
    @ResponseBody VkDatasetExploration lastExplorationStatus() {
        explorationService.getLastExploration()
    }

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
