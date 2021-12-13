---
layout: demopage
title: Spark NLP in Action
full_width: true
permalink: /analyze_non_english_medical_text
key: demo
license: false
show_edit_on_github: false
show_date: false
data:
  sections:  
    - title: Spark NLP for Healthcare 
      excerpt: Analyze non-English Medical Text
      secheader: yes
      secheader:
        - title: Spark NLP for Healthcare
          subtitle: Analyze non-English Medical Text
          activemenu: analyze_non_english_medical_text
      source: yes
      source: 
        - title: ICD-10 taxonomy for German medical terminology
          hide: yes
          id: icd10_coding_for_german
          image: 
              src: /assets/images/Detect_diagnosis_and_procedures.svg
          image2: 
              src: /assets/images/Detect_diagnosis_and_procedures_f.svg
          excerpt: Automatically detect the pre and post op diagnosis, signs and symptoms in your German healthcare records and automatically link them to the corresponding ICD10-CM code using Spark NLP for Healthcare out of the box.
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/healthcare/ER_ICD10_GM_DE/
          - text: Colab Netbook
            type: blue_btn
            url: https://colab.research.google.com/github/JohnSnowLabs/spark-nlp-workshop/blob/master/tutorials/streamlit_notebooks/healthcare/ER_ICD10_GM_DE.ipynb
        - title: Detect symptoms, treatments and other NERs in German
          id: detect_symptoms
          image: 
              src: /assets/images/Detect_causality_between_symptoms.svg
          image2: 
              src: /assets/images/Detect_causality_between_symptoms_f.svg
          excerpt: Automatically identify entities such as symptoms, diagnoses, procedures, body parts or medication in German clinical text using the pretrained Spark NLP clinical model ner_healthcare.
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/healthcare/NER_HEALTHCARE_DE/
          - text: Colab Netbook
            type: blue_btn
            url: https://colab.research.google.com/github/JohnSnowLabs/spark-nlp-workshop/blob/master/tutorials/streamlit_notebooks/healthcare/NER_HEALTHCARE_DE.ipynb
        - title: Detect legal entities in German
          id: detect_legal_entities_german
          image: 
              src: /assets/images/Grammar_Analysis.svg
          image2: 
              src: /assets/images/Grammar_Analysis_f.svg
          excerpt: Automatically identify entities such as persons, judges, lawyers, countries, cities, landscapes, organizations, courts, trademark laws, contracts, etc. in German legal text using the pretrained Spark NLP models ner_legal.
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/healthcare/NER_LEGAL_DE/
          - text: Colab Netbook
            type: blue_btn
            url: https://colab.research.google.com/github/JohnSnowLabs/spark-nlp-workshop/blob/master/tutorials/streamlit_notebooks/healthcare/NER_LEGAL_DE.ipynb
        - title: Detect traffic information in text
          id: detect_traffic_information_in_text
          image: 
              src: /assets/images/Detect_traffic_information_in_text.svg
          image2: 
              src: /assets/images/Detect_traffic_information_in_text_f.svg
          excerpt: Automatically extract geographical location, postal codes, and traffic routes in German text using our pretrained Spark NLP model.
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/healthcare/NER_TRAFFIC_DE/
          - text: Colab Netbook
            type: blue_btn
            url: https://colab.research.google.com/github/JohnSnowLabs/spark-nlp-workshop/blob/master/tutorials/streamlit_notebooks/healthcare/CLINICAL_NER.ipynb
        - title: Detect Diagnoses And Procedures In Spanish
          id: detect-diagnoses-and-procedures-in-spanish
          image: 
              src: /assets/images/Detect_drugs_and_prescriptions.svg
          image2: 
              src: /assets/images/Detect_drugs_and_prescriptions_f.svg
          excerpt: Automatically identify diagnoses and procedures in Spanish clinical documents using the pre-trained Spark NLP clinical model.
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/healthcare/NER_DIAG_PROC_ES/
          - text: Colab Netbook
            type: blue_btn
            url: https://githubtocolab.com/JohnSnowLabs/spark-nlp-workshop/blob/master/tutorials/streamlit_notebooks/healthcare/NER_DIAG_PROC_ES.ipynb
        - title: HPO coding - Spanish
          id: hpo_coding_spanish
          image: 
              src: /assets/images/HPO_coding_Spanish.svg
          image2: 
              src: /assets/images/HPO_coding_Spanish_f.svg
          excerpt: Entity Resolver for Human Phenotype Ontology in Spanish
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/healthcare/ER_HPO_ES/
          - text: Colab Netbook
            type: blue_btn
            url: https://colab.research.google.com/github/JohnSnowLabs/spark-nlp-workshop/blob/master/tutorials/Certification_Trainings/Healthcare/24.Improved_Entity_Resolvers_in_SparkNLP_with_sBert.ipynb
        - title: Named Entity Recognition for (Brazilian) Portuguese Legal Texts
          id: named_entity_recognition_for_portuguese_legal_texts
          image: 
              src: /assets/images/Named_Entity_Recognition_for_Portuguese_Legal_Texts.svg
          image2: 
              src: /assets/images/Named_Entity_Recognition_for_Portuguese_Legal_Texts_f.svg
          excerpt: Recognize Organization, Jurisprudence, Legislation, Person, Location, and Time in legal texts (Brazilian Portuguese) using pre-trained Spark NLP model with Bert Embeddings
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/healthcare/NER_LENER/
          - text: Colab Netbook
            type: blue_btn
            url: 
        - title: Detect professions and occupations in Spanish texts
          id: detect_professions_occupations_Spanish_texts 
          image: 
              src: /assets/images/Classify-documents.svg
          image2: 
              src: /assets/images/Classify-documents-w.svg
          excerpt: Automatically identify professions and occupations entities in Spanish texts using our pretrained Spark NLP for Healthcare model. 
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/healthcare/NER_PROFESSIONS_ES/ 
          - text: Colab Netbook
            type: blue_btn
            url:
        - title: Detect Tumor Characteristics in Spanish medical texts
          id: detect_tumor_characteristics_spanish_medical_texts  
          image: 
              src: /assets/images/Detect_Tumor_Characteristics_in_Spanish_medical_texts.svg
          image2: 
              src: /assets/images/Detect_Tumor_Characteristics_in_Spanish_medical_texts_f.svg
          excerpt: This demo shows how tumor characteristics (morphology) in Spanish medical texts can be detected automatically using Spark NLP NER model.
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/healthcare/NER_TUMOR_ES/  
          - text: Colab Netbook
            type: blue_btn
            url: https://colab.research.google.com/github/JohnSnowLabs/spark-nlp-workshop/blob/master/tutorials/streamlit_notebooks/healthcare/NER_TUMOR_ES.ipynb 
        
---