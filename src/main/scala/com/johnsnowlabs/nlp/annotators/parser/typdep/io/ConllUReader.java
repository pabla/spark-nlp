package com.johnsnowlabs.nlp.annotators.parser.typdep.io;

import com.johnsnowlabs.nlp.annotators.parser.typdep.DependencyInstance;

import java.io.IOException;
import java.util.ArrayList;

public class ConllUReader extends DependencyReader{

     /**
	     *  CoNLL Universal Dependency format:
		    0 ID
		    1 FORM
		    2 LEMMA
		    3 UPOS
		    4 XPOS
		    5 FEATS
		    6 HEAD
		    7 DEPREL
		    8 MISC
	   	*/

    @Override
    public DependencyInstance nextInstance() throws IOException {

        ArrayList<String[]> lstLines = getFileContentAsArray();

        if (lstLines.isEmpty()) {
            return null;
        }

        int length = lstLines.size();
        String[] forms = new String[length + 1];
        String[] lemmas = new String[length + 1];
        String[] cpos = new String[length + 1];
        String[] pos = new String[length + 1];
        String[][] feats = new String[length + 1][];
        String[] deprels = new String[length + 1];
        int[] heads = new int[length + 1];

        forms[0] = "<root>";
        lemmas[0] = "<root-LEMMA>";
        pos[0] = "<root-POS>";
        cpos[0] = pos[0];
        deprels[0] = "<no-type>";
        heads[0] = -1;

        boolean hasLemma = false;

        for (int i = 1; i < length + 1; ++i) {
            String[] parts = lstLines.get(i-1);
            forms[i] = parts[1];
            if (!parts[2].equals("_")) {
                lemmas[i] = parts[2];
                hasLemma = true;
            }

            pos[i] = parts[3];
            cpos[i] = pos[i];

            if (!parts[5].equals("_")) {
                feats[i] = parts[5].split("\\|");
            }

            if (parts[6].equals("_")) {
                System.out.println("Error in sentence:\n");
                System.out.println(parts[0] + " " + parts[1] + " " +  parts[2] + " " + parts[3]);
            }

            heads[i] = Integer.parseInt(parts[6]);
            deprels[i] = parts[7];

        }
        if (!hasLemma) lemmas = null;

        return new DependencyInstance(forms, lemmas, cpos, pos, feats, heads, deprels, null, null);
    }

    private ArrayList<String[]> getFileContentAsArray() throws IOException {

        ArrayList<String[]> lstLines = new ArrayList<>();

        String line = reader.readLine();
        while (line != null && !line.equals("")) {
            if (!line.startsWith("#")) {
                int endIndex = line.indexOf('\t');
                String id = line.substring(0, endIndex);
                if (!id.contains(".")) {
                    lstLines.add(line.trim().split("\t"));
                }
            }
            line = reader.readLine();
        }
        return lstLines;
    }


}
