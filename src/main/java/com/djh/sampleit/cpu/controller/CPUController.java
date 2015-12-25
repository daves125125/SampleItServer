package com.djh.sampleit.cpu.controller;

import com.djh.sampleit.cpu.controller.model.CPUCore;
import com.djh.sampleit.cpu.controller.model.CPUMetric;
import com.djh.sampleit.cpu.model.CPUSample;
import com.djh.sampleit.cpu.service.CPUService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David Hancock
 */
@RestController
public class CPUController {

    private static final Logger LOG = LoggerFactory.getLogger(CPUController.class);

    @Autowired
    private CPUService cpuService;

    @RequestMapping(value = "cpu", method = RequestMethod.POST)
    public void cpuMetric(@RequestBody CPUMetric cpuMetric) {

        LOG.info("Received metric: " + cpuMetric.toString());
        cpuService.saveCPUMetric(cpuMetric);
    }

    @CrossOrigin
    @RequestMapping(value = "cpu/samples/{hostname}", method = RequestMethod.GET)
    public List<List<Double>> cpuOverlayForHost(@PathVariable String hostname) {

        List<List<Double>> cpuStats = new ArrayList<>();

        List<CPUSample> allCpuSamples = cpuService.retrieveLatestCPUSampleSetsForHostname(hostname);
        if (allCpuSamples != null && !allCpuSamples.isEmpty()) {

            // TODO This truncation certainly needs to be done sooner.
            if (allCpuSamples.size() > 30) {
                cpuStats = transformCpuStats(allCpuSamples.subList(
                        allCpuSamples.size() - 30, allCpuSamples.size()));
            } else {
                cpuStats = transformCpuStats(allCpuSamples);
            }
        }

        return cpuStats;
    }

    /**
     * TODO Maybe move this down into the service.
     * <p>
     * Transforms an indeterminate list of CPU values into set of lists, each representing
     * clock speed per core.
     */
    private List<List<Double>> transformCpuStats(List<CPUSample> cpuSamples) {

        List<List<Double>> cpuStats = new ArrayList<>();

        int numberOfCores = cpuSamples.get(0).getCpuCores().size();

        for (int i = 0; i < numberOfCores; i++) {
            cpuStats.add(new ArrayList<>());
        }

        for (CPUSample cpuSample : cpuSamples) {
            List<CPUCore> cpuCores = cpuSample.getCpuCores();

            for (int i = 0; i < numberOfCores; i++) {
                cpuStats.get(i).add(cpuCores.get(i).getCpuClockSpeed());
            }
        }

        return cpuStats;
    }

}
