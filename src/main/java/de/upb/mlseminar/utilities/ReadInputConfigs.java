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

public class ReadInputConfigs {

	private static final Logger logger = LoggerFactory.getLogger(InformedMonteCarloPlayer.class);
	
	public static List<ModelInputConfig> readConfigFile(String configFile) {
		
		List<ModelInputConfig> inputConfigsList =  new ArrayList<>();
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
				inputConfig.setMinNumOfPlacements(Integer.parseInt(values[3]));
				
				inputConfigsList.add(inputConfig);
            }
             br.close();
        } catch (FileNotFoundException e) {
            logger.error("File not found");
        } catch (IOException e) {
        	logger.error("Unable to read the file.");
        }
     return inputConfigsList;
}
}
