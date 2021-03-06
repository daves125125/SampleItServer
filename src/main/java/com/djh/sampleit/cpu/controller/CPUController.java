package com.djh.sampleit.cpu.controller;

import com.djh.sampleit.cpu.controller.model.CPUCore;
import com.djh.sampleit.cpu.controller.model.CPUMetric;
import com.djh.sampleit.cpu.model.CPUSample;
import com.djh.sampleit.cpu.service.CPUService;
import com.djh.sampleit.visitor.dao.SimpleVisitorDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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

    // TODO Obviously move this elsewhere
    @Autowired
    private SimpleVisitorDAO simpleVisitorDAO;


    @RequestMapping(value = "cpu", method = RequestMethod.POST)
    public void postCpuMetric(@RequestBody CPUMetric cpuMetric, HttpServletRequest httpServletRequest) {

        // TODO AOP this maybe.
        String originIPAddress = httpServletRequest.getRemoteAddr();
        cpuMetric.getMetricMetadata().setOriginPublicIPAddress(originIPAddress);

        LOG.info("Received CPU Metric: " + cpuMetric.toString());
        cpuService.saveCPUMetric(cpuMetric);
    }

    @CrossOrigin
    @RequestMapping(value = "cpu/samples/{machineAlias}/{numberOfPoints}", method = RequestMethod.GET)
    public List<List<Double>> getCpuOverlayForHost(@PathVariable String machineAlias,
                                                   @PathVariable int numberOfPoints,
                                                   HttpServletRequest httpServletRequest) {

        // TODO Obviously move this elsewhere
        simpleVisitorDAO.addVisitor(httpServletRequest.getRemoteAddr());

        List<List<Double>> cpuStats = new ArrayList<>();

        List<CPUSample> allCpuSamples = cpuService.retrieveCPUSampleSetsForMachine(machineAlias);
        if (allCpuSamples != null && !allCpuSamples.isEmpty()) {

            // TODO This truncation certainly needs to be done sooner.
            if (allCpuSamples.size() > numberOfPoints) {
                cpuStats = transformCpuStats(allCpuSamples.subList(
                        allCpuSamples.size() - numberOfPoints, allCpuSamples.size()));
            } else {
                cpuStats = transformCpuStats(allCpuSamples);
            }
        }

        return cpuStats;
    }

    /**
     * TODO Definitely move this down into the service.
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
