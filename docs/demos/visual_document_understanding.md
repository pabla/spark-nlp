---
layout: demopage
title: Spark NLP in Action
full_width: true
permalink: /visual_document_understanding
key: demo
license: false
show_edit_on_github: false
show_date: false
data:
  sections:  
    - title: Spark OCR
      excerpt: Visual Document Understanding
      secheader: yes
      secheader:
        - title: Spark OCR
          subtitle: Visual Document Understanding
          activemenu: visual_document_understanding
      source: yes
      source: 
        - title: Visual Document Classification
          id: classify_visual_documents
          image: 
              src: /assets/images/Classify_visual_documents.svg
          image2: 
              src: /assets/images/Classify_visual_documents_f.svg
          excerpt: Classify documents using text and layout data with the new features offered by Spark OCR.
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/ocr/VISUAL_DOCUMENT_CLASSIFY/
          - text: Colab Netbook
            type: blue_btn
            url: https://colab.research.google.com/github/JohnSnowLabs/spark-ocr-workshop/blob/master/jupyter/SparkOCRVisualDocumentClassifier.ipynb
        # - title: Signature Detection
        #   id: extract_signatures 
        #   image: 
        #       src: /assets/images/Extract_Signatures.svg
        #   image2: 
        #       src: /assets/images/Extract_Signatures_c.svg
        #   excerpt: This demo shows how handwritten signatures can be extracted from image/pdf documents using Spark OCR.
        #   actions:
        #   - text: Live Demo
        #     type: normal
        #     url: https://demo.johnsnowlabs.com/ocr/DETECT_SIGNATURES/
        #   - text: Colab Netbook
        #     type: blue_btn
        #     url: https://colab.research.google.com/github/JohnSnowLabs/spark-ocr-workshop/blob/3.6.0/jupyter/SparkOcrImageSignatureDetection.ipynb
        - title: Extract Data from Scanned Invoices
          id: extract_entities_from_visual_documents  
          image: 
              src: /assets/images/Extract_entities_from_visual_documents.svg
          image2: 
              src: /assets/images/Extract_entities_from_visual_documents_c.svg
          excerpt: Detect companies, total amounts and dates in scanned invoices using out of the box Spark OCR models. 
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/ocr/VISUAL_DOCUMENT_NER/
          - text: Colab Netbook
            type: blue_btn
            url: https://colab.research.google.com/github/JohnSnowLabs/spark-ocr-workshop/blob/master/jupyter/SparkOCRVisualDocumentNer.ipynb
        - title: Extract Data from FoundationOne Sequencing Reports
          id: extract-data-from-foundationone-sequencing-reports
          image: 
              src: /assets/images/correct.svg
          image2: 
              src: /assets/images/correct_f.svg
          excerpt: Extract patient, genomic and biomarker information from FoundationOne Sequencing Reports.
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/ocr/FOUNDATIONONE_REPORT_PARSING/
          - text: Colab Netbook
            type: blue_btn
            url:   
        - title: Recognize entities in scanned PDFs
          id: recognize_entities_in_scanned_pdfs
          image: 
              src: /assets/images/Recognize_text_in_natural_scenes.svg
          image2: 
              src: /assets/images/Recognize_text_in_natural_scenes_f.svg
          excerpt: 'End-to-end example of regular NER pipeline: import scanned images from cloud storage, preprocess them for improving their quality, recognize text using Spark OCR, correct the spelling mistakes for improving OCR results and finally run NER for extracting entities.'
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/ocr/PDF_TEXT_NER/
          - text: Colab Netbook
            type: blue_btn
            url: https://colab.research.google.com/github/JohnSnowLabs/spark-nlp-workshop/blob/master/tutorials/streamlit_notebooks/ocr/PDF_TEXT_NER.ipynb     
---
