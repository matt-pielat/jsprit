package pl.pielat.benchmark.algorithmCreation;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import pl.pielat.algorithm.ExtendedProblemDefinition;
import pl.pielat.algorithm.GarridoRiff;

public class GarridoRiffAlgorithmFactory extends AlgorithmFactory
{
    private int populationSize;
    private int offspringSize;
    private int chromosomeSize;

    public GarridoRiffAlgorithmFactory(int populationSize, int offspringSize, int chromosomeSize)
    {
        this.populationSize = populationSize;
        this.offspringSize = offspringSize;
        this.chromosomeSize = chromosomeSize;
    }

    @Override
    public String getSerializableAlgorithmId()
    {
        return "GarridoRiff";
    }

    @Override
    public VehicleRoutingAlgorithm createAlgorithm(ExtendedProblemDefinition vrp)
    {
        GarridoRiff gr = new GarridoRiff();
        gr.setPopulationSize(populationSize);
        gr.setOffspringSize(offspringSize);
        gr.setChromosomeSize(chromosomeSize);

        return gr.createAlgorithm(vrp);
    }
}
