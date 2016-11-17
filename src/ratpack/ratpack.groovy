import groovy.json.JsonSlurper
import ratpack.http.MutableHeaders

import static ratpack.groovy.Groovy.ratpack

ratpack {
    handlers {
        get('explore') {
            MutableHeaders headers = response.headers
            headers.set('Access-Control-Allow-Origin', '*')
            headers.set('Access-Control-Allow-Headers', 'x-requested-with, origin, content-type, accept')
            headers.set('content-type', 'application/json')

            String inputFile = '/data/vk.data'

            File dataFile = new File(inputFile)
            String dataAsText = "[ ${dataFile.text.split('\n').join(', ')} ]"
            def dataAsJson = new JsonSlurper().parseText(dataAsText)

            render """{
    "size": {
      "total": ${dataAsJson.size()},
      "unique": ${dataAsJson.collect({ it.user_id }).unique().size()}
    },
    "country": {
      "count": ${dataAsJson.collect({ it.country }).findAll({it > 0}).unique().size()},
      "undefined": ${dataAsJson.collect({ it.country }).findAll({it == 0}).size()},
      "distribution": ${dataAsJson.collect({ it.country }).findAll({it > 0}).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> '{ "name": "' + key + '", "count": ' + value.size() + ' }' }).take(10)}
    },
    "city": {
      "count": ${dataAsJson.collect({ it.city }).findAll({it > 0}).unique().size()},
      "undefined": ${dataAsJson.collect({ it.cities }).findAll({it == 0}).size()},
      "distribution": ${dataAsJson.collect({ it.city }).findAll({it > 0}).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> [ '{ "name": "' + key + '", "count": ' + value.size() + ' }' ] }).take(10)}
    },
    "university": {
      "count": ${dataAsJson.collect({ it.university_name }).findAll({it != ''}).unique().size()},
      "undefined": ${dataAsJson.collect({ it.university_name }).findAll({it == ''}).size()},
      "distribution": ${dataAsJson.collect({ it.university_name }).findAll({it != ''}).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> [ '{ "name": "' + key + '", "count": ' + value.size() + ' }' ] }).take(10)}
    },
    "faculty": {
      "count": ${dataAsJson.collect({ it.faculty_name }).findAll({it != ''}).unique().size()},
      "undefined": ${dataAsJson.collect({ it.faculty_name }).findAll({it == ''}).size()},
      "distribution": ${dataAsJson.findAll({it.faculty_name != ''}).collect({ "${it.faculty_name} ${it.university_name}".trim() }).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> [ '{ "name": "' + key + '", "count": ' + value.size() + ' }' ] }).take(10)}
    },
    "graduateYear": {
      "count": ${dataAsJson.collect({ it.graduation }).findAll({it > 0}).unique().size()},
      "undefined": ${dataAsJson.collect({ it.graduation }).findAll({it == 0}).size()},
      "distribution": ${dataAsJson.collect({ it.graduation }).findAll({it > 0}).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> [ '{ "name": "' + key + '", "count": ' + value.size() + ' }' ] }).take(10)}
    },
    "occupation": {
      "type": {
        "count": ${dataAsJson.collect({ it?.occupation?.type }).unique().size()},
        "distribution": ${dataAsJson.collect({ it?.occupation?.type }).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> [ '{ "name": "' + key + '", "count": ' + value.size() + ' }' ] }).take(10)}
      },
      "name": {
        "count": ${dataAsJson.collect({ it?.occupation?.name }).unique().size()},
        "distribution": ${dataAsJson.collect({ it?.occupation?.name }).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> [ '{ "name": "' + key + '", "count": ' + value.size() + ' }' ] }).take(10)}
      }
    },
    "work": {
      "name": {
        "count": ${dataAsJson.findAll({ it?.occupation?.type == 'work' && !it?.occupation?.name?.contains('Эта страница официально подтверждена') }).collect({ it?.occupation?.name }).unique().size()},
        "distribution": ${dataAsJson.findAll({ it?.occupation?.type == 'work' && !it?.occupation?.name?.contains('Эта страница официально подтверждена') }).collect({ it?.occupation?.name }).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> [ '{ "name": "' + key + '", "count": ' + value.size() + ' }' ] }).take(10)}
      }
    },
    "career": {
      "size": {
        "distribution": ${dataAsJson.collect({ it.career.size() }).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> [ '{ "name": "' + key + '", "count": ' + value.size() + ' }' ] }).take(10)}
      },
      "company": {
        "count": ${dataAsJson.collect({ it.career.collect({it.company}) }).flatten().findAll({it != null}).unique().size()},
        "distribution": ${dataAsJson.collect({ it.career.collect({it.company}) }).flatten().findAll({it != null}).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> [ '{ "name": "' + key + '", "count": ' + value.size() + ' }' ] }).take(10)}
      },
      "city": {
        "count": ${dataAsJson.collect({ it.career.collect({it.city_id}) }).flatten().findAll({it != null}).unique().size()},
        "distribution": ${dataAsJson.collect({ it.career.collect({it.city_id}) }).flatten().findAll({it != null}).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> [ '{ "name": "' + key + '", "count": ' + value.size() + ' }' ] }).take(10)}
      },
      "position": {
        "count": ${dataAsJson.collect({ it.career.collect({it.position?.toLowerCase()}) }).flatten().findAll({it != null}).unique().size()},
        "distribution": ${dataAsJson.collect({ it.career.collect({it.position?.toLowerCase()}) }).flatten().findAll({it != null}).groupBy({ it }).sort({ -it.value.size() }).collect({ key, value -> [ '{ "name": "' + key + '", "count": ' + value.size() + ' }' ] }).take(10)}
      }
    }
}"""
        }
    }
}