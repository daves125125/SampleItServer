package com.djh.sampleit.cpu.service;

import com.djh.sampleit.cpu.controller.model.CPUSample;
import com.djh.sampleit.cpu.controller.model.CPUSampleSet;
import com.djh.sampleit.cpu.dao.CPUSampleDAO;
import com.djh.sampleit.cpu.controller.model.CPUMetric;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David Hancock
 */
public class DefaultCPUService implements CPUService {

    @Autowired
    private CPUSampleDAO cpuSampleDAO;

    @Override
    public void saveCPUMetric(CPUMetric cpuMetric) {

        // TODO Do Transform here from Metric > Sample
        CPUSample cpuSample = new CPUSample();
        cpuSample.setCpuCores(cpuMetric.getCpuCores());
        cpuSample.setTimestamp(cpuMetric.getMetricMetadata().getDate());

        String hostname = cpuMetric.getMetricMetadata().getHostname();
        cpuSampleDAO.persistCPUSample(hostname, cpuSample);
    }

    @Override
    public CPUSampleSet retrieveCPUSampleSet(String hostname) {

        List<CPUSample> cpuSamples = cpuSampleDAO.readAllCPUSamplesForHostname(hostname);

        CPUSampleSet cpuSampleSet = new CPUSampleSet();
        cpuSampleSet.setHostname(hostname);
        cpuSampleSet.setCpuSamples(cpuSamples);

        return cpuSampleSet;
    }

    @Override
    public List<CPUSampleSet> retrieveAllCPUSampleSets() {
        return cpuSampleDAO.readAllCPUSamples();
    }

    @Override
    public List<CPUSampleSet> retrieveLatestCPUSampleSets() {
        return cpuSampleDAO.readLatestCPUSamples();
    }


}