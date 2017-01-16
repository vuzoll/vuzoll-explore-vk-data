package com.github.vuzoll.explorevk.controller

import com.github.vuzoll.explorevk.services.ExploreVkService
import com.github.vuzoll.explorevk.services.VkExploration
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@Slf4j
class ExploreVkController {

    @Autowired
    ExploreVkService exploreVkService

    @GetMapping(path = '/explore')
    @ResponseBody VkExploration explore() {
        log.info 'Receive exploration request'

        exploreVkService.explore()
    }
}
