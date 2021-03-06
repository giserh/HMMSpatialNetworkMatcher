package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl;

import java.util.Random;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.CompositeEmissionProbabilityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.ITransitionProbabilityStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.PathBuilder;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.PostProcessStrategy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.ep.FrechetEmissionProbability;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.io.HMMExporter;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.pathbuilder.StrokePathBuilder;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.postProcessStrategy.OptimizationPostStratregy;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.HMMMatchingLauncher;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.ParametersSet;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.tp.AngularTransitionProbability;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class LaunchExemple {

  public static void main(String[] args) {
    // Disable Logger messages
    Logger.getRootLogger().setLevel(Level.OFF);

    // First network

//    String fileNetwork1 ="./manual_matching/snapshot_1784.0_1791.0_edges.shp";
    String fileNetwork1 ="./DEP29/cassini_29241_modified.shp";
    String id1 = "edge_id";

    // Second network
//    String fileNetwork2 ="./manual_matching/snapshot_1825.0_1836.0_edges.shp";
    String fileNetwork2 ="./DEP29/etat_major_29241.shp";
    String id2 = "ID";

    // Emission probability stratregy
    // If you want to use more than one criteria, use CompositeEmissionProbability to wrap them
    CompositeEmissionProbabilityStrategy epStrategy = new CompositeEmissionProbabilityStrategy();
    // epStrategy.add(new LineMedianDistanceEmissionProbability(), 1.);
    // epStrategy.add(new DirectionDifferenceEmissionProbability(), 1.);
    // parameter of the exponential distribution of Frechet proba
    double lamdaFrechet = 1.;
    epStrategy.add(new FrechetEmissionProbability(lamdaFrechet), 1.);

    // Transition probability Strategy
    // parameter of the exponential distribution of Angular transition proba
    double lamdaAngular = 1.;
    ITransitionProbabilityStrategy tpStrategy = new AngularTransitionProbability(lamdaAngular);

    // How to build the paths of the HMM ?
    PathBuilder pathBuilder = new StrokePathBuilder();

    // How to manage unexpected matched entities ?
    PostProcessStrategy postProcressStrategy = new OptimizationPostStratregy();

    // Parameters of the algorithm
    ParametersSet.get().SELECTION_THRESHOLD = 100;
    ParametersSet.get().NETWORK_PROJECTION = true;
    ParametersSet.get().PATH_MIN_LENGTH = 7;
    
    // Start the parallelized matching
    boolean parallelProcess = false;

    Random generator = new Random(42L);
    // Launcher
    HMMMatchingLauncher matchingLauncher = new HMMMatchingLauncher(fileNetwork1, fileNetwork2, epStrategy,
        tpStrategy, pathBuilder, postProcressStrategy, parallelProcess, generator);

    long start = System.currentTimeMillis();
    // Execute the HMM matching algorithm
    matchingLauncher.lauchMatchingProcess();
    long end = System.currentTimeMillis();
    // Export result
//    matchingLauncher.exportMatchingResults("cassini_em");
    HMMExporter export = new HMMExporter();
    IFeatureCollection<IFeature> e = export.exportFeaturesWithID(matchingLauncher.getMatching(), ShapefileReader.read(fileNetwork1), ShapefileReader.read(fileNetwork2), id1, id2);
    ShapefileWriter.write(e, "/home/julien/data/DEP29/cassini_em_29241.shp");
    System.out.println("Matching took " + (end - start) + " ms");
  }
}
