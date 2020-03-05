package de.upb.mlseminar.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.mlseminar.mymcts.montecarlo.InformedMonteCarloPlayer;

/**
 * This class reads the state configs from a textfile
 * and returns a ModelConfig object which is used for generating child nodes of MCTS approach
 *
 * @author Guru Prasad Savandaiah
 *
 */
public class ReadInputConfigs {

	private static final Logger logger = LoggerFactory.getLogger(InformedMonteCarloPlayer.class);
	
	public static List<ModelInputConfig> readConfigFile(String configFile) {
		
		List<ModelInputConfig> inputConfigsList =  new ArrayList<>();
        
        ModelInputConfig inputConfig1 = new ModelInputConfig(3,3,10,0,3);
        inputConfigsList.add(inputConfig1);
        
        ModelInputConfig inputConfig2 = new ModelInputConfig(5,3,5,0,3);
        inputConfigsList.add(inputConfig2);
        
        ModelInputConfig inputConfig3 = new ModelInputConfig(3,5,10,0,3);
        inputConfigsList.add(inputConfig3);
        
        ModelInputConfig inputConfig4 = new ModelInputConfig(3,5,20,0,3);
        inputConfigsList.add(inputConfig4);
        
        ModelInputConfig inputConfig5 = new ModelInputConfig(5,5,20,0,3);
        inputConfigsList.add(inputConfig5);
        
        ModelInputConfig inputConfig6 = new ModelInputConfig(5,5,10,0,3);
        inputConfigsList.add(inputConfig6);
        
        ModelInputConfig inputConfig7 = new ModelInputConfig(3,3,5,2,3);
        inputConfigsList.add(inputConfig7);
        
        ModelInputConfig inputConfig8 = new ModelInputConfig(5,3,3,5,3);
        inputConfigsList.add(inputConfig8);
        
        ModelInputConfig inputConfig9 = new ModelInputConfig(3,5,2,5,2);
        inputConfigsList.add(inputConfig9);
        
        ModelInputConfig inputConfig10 = new ModelInputConfig(3,5,6,2,3);
        inputConfigsList.add(inputConfig10);
        
        ModelInputConfig inputConfig11 = new ModelInputConfig(5,5,7,4,3);
        inputConfigsList.add(inputConfig11);
        
        ModelInputConfig inputConfig12 = new ModelInputConfig(5,5,20,0,2);
        inputConfigsList.add(inputConfig12);
        
        ModelInputConfig inputConfig13 = new ModelInputConfig(5,5,10,2,4);
        inputConfigsList.add(inputConfig13);
        
        ModelInputConfig inputConfig14 = new ModelInputConfig(5,5,10,2,5);
        inputConfigsList.add(inputConfig14);
        
        ModelInputConfig inputConfig15 = new ModelInputConfig(5,5,20,2,4);
        inputConfigsList.add(inputConfig15);
        
        /*
        ClassLoader classLoader = new ReadInputConfigs().getClass().getClassLoader();
        File file = new File(classLoader.getResource(configFile).getFile());
        String strLine = "";
        String DELIMITER = ",";
        List<List<String>> runConfigs = new ArrayList<>();
        try {
             BufferedReader br = new BufferedReader(new FileReader(file));
             br.readLine(); // this will read the first line
             strLine=null;
              while ((strLine = br.readLine()) != null)
               {
            	ModelInputConfig inputConfig = new ModelInputConfig();
				String[] values = strLine.split(DELIMITER);
				runConfigs.add(Arrays.asList(values));
				
				inputConfig.setOwnDiscardPileThreshold(Integer.parseInt(values[0]));
				inputConfig.setOwnDiscardPileIncreamentFactor(Integer.parseInt(values[1]));
				inputConfig.setOpponentDiscardPileThreshold(Integer.parseInt(values[2]));
				inputConfig.setOpponentDiscardPileIncreamentFactor(Integer.parseInt(values[3]));
				inputConfig.setMinNumOfPlacements(Integer.parseInt(values[4]));
				
				inputConfigsList.add(inputConfig);
            }
             br.close();
        } catch (Exception e) {
            logger.error("File not found");
			e.printStackTrace();
			
        }
        */
     return inputConfigsList;
}
}
