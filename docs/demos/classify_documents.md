---
layout: demopage
title: Spark NLP in Action
full_width: true
permalink: /classify_documents
key: demo
license: false
show_edit_on_github: false
show_date: false
data:
  sections:  
    - title: Spark NLP - English
      excerpt: Classify Documents
      secheader: yes
      secheader:
        - title: Spark NLP - English
          subtitle: Classify Documents
          activemenu: classify_documents
      source: yes
      source: 
        - title: Classify documents
          id: classify_documents
          image: 
              src: /assets/images/Classify-documents.svg
          image2: 
              src: /assets/images/Classify-documents-w.svg
          excerpt: Classify open-domain, fact-based questions into one of the following broad semantic categories <b>Abbreviation, Description, Entities, Human Beings, Locations or Numeric Values</b>
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/public/CLASSIFICATION_EN_TREC/
          - text: Colab Netbook
            type: blue_btn
            url: https://colab.research.google.com/github/JohnSnowLabs/spark-nlp-workshop/blob/master/tutorials/streamlit_notebooks/CLASSIFICATION_EN_TREC.ipynb
        - title: Identify Fake news
          id: identify_fake_news
          image: 
              src: /assets/images/fake-news.svg
          image2: 
              src: /assets/images/fake-news-w.svg
          excerpt: Determine if news articles are <b>Real</b> of <b>Fake</b>.
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/public/CLASSIFICATION_EN_FAKENEWS/
          - text: Colab Netbook
            type: blue_btn
            url: https://colab.research.google.com/github/JohnSnowLabs/spark-nlp-workshop/blob/master/tutorials/streamlit_notebooks/CLASSIFICATION_EN_FAKENEWS.ipynb
        - title: Detect Spam messages
          id: detect_spam_messages
          image: 
              src: /assets/images/exclamation.svg
          image2: 
              src: /assets/images/exclamation-w.svg
          excerpt: Automatically identify messages as being regular messages or <b>Spam</b>.
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/public/CLASSIFICATION_EN_SPAM/
          - text: Colab Netbook
            type: blue_btn
            url: https://colab.research.google.com/github/JohnSnowLabs/spark-nlp-workshop/blob/master/tutorials/streamlit_notebooks/CLASSIFICATION_EN_SPAM.ipynb
        - title: Detect toxic content in comments
          id: detect_toxic_content_in_comments
          image: 
              src: /assets/images/Detect_Toxic_Comments.svg
          image2: 
              src: /assets/images/Detect_Toxic_Comments_f.svg
          excerpt: Automatically detect identity hate, insult, obscene, severe toxic, threat or toxic content in SM comments using our out-of-the-box Spark NLP Multiclassifier DL.
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/public/CLASSIFICATION_MULTILABEL_TOXIC/
          - text: Colab Netbook
            type: blue_btn
            url: https://colab.research.google.com/github/JohnSnowLabs/spark-nlp-workshop/blob/master/tutorials/streamlit_notebooks/CLASSIFICATION_MULTILABEL_TOXIC.ipynb
        - title: Detect sarcastic tweets
          id: detect_sarcastic_tweets
          image: 
              src: /assets/images/Detect-sarcastic-tweets.svg
          image2: 
              src: /assets/images/Detect-sarcastic-tweets-w.svg
          excerpt: Checkout our sarcasm detection pretrained Spark NLP model. It is able to tell apart normal content from sarcastic content.
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/public/SENTIMENT_EN_SARCASM/
          - text: Colab Netbook
            type: blue_btn
            url: https://colab.research.google.com/github/JohnSnowLabs/spark-nlp-workshop/blob/master/tutorials/streamlit_notebooks/SENTIMENT_EN_SARCASM.ipynb
        - title: Identify whether pairs of questions are semantically similar 
          id: identify_whether_pairs_questions_semantically_similar 
          image: 
              src: /assets/images/Identify_whether_pairs_of_questions_are_semantically_similar.svg
          image2: 
              src: /assets/images/Identify_whether_pairs_of_questions_are_semantically_similar_f.svg
          excerpt: This demo shows whether the two question sentences are semantically repetitive or different.
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/public/CLASSIFICATION_QUESTIONPAIR/
          - text: Colab Netbook
            type: blue_btn
            url: https://colab.research.google.com/github/JohnSnowLabs/spark-nlp-workshop/blob/master/tutorials/streamlit_notebooks/CLASSIFICATION_QUESTIONPAIRS.ipynb
        - title: Identify sentiments in German texts.
          id: identify_sentiments_German_texts 
          image: 
              src: /assets/images/Identify_sentiments_in_Geman_texts.svg
          image2: 
              src: /assets/images/Identify_sentiments_in_Geman_texts_f.svg
          excerpt: This demo shows whether the sentiments are positive or negative in German texts. 
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/public/SENTIMENT_DE/
          - text: Colab Netbook
            type: blue_btn
            url: https://colab.research.google.com/github/JohnSnowLabs/spark-nlp-workshop/blob/master/tutorials/streamlit_notebooks/CLASSIFICATION_De_SENTIMENT.ipynb
        - title: Identify sentiments in French texts.
          id: identify_sentiments_french_texts 
          image: 
              src: /assets/images/Identify_sentiments_in_French_texts.svg
          image2: 
              src: /assets/images/Identify_sentiments_in_French_texts_f.svg
          excerpt: This demo shows whether the sentiments are positive or negative in French texts. 
          actions:
          - text: Live Demo
            type: normal
            url: https://demo.johnsnowlabs.com/public/SENTIMENT_FR/
          - text: Colab Netbook
            type: blue_btn
            url: https://colab.research.google.com/github/JohnSnowLabs/spark-nlp-workshop/blob/master/tutorials/streamlit_notebooks/CLASSIFICATION_Fr_Sentiment.ipynb
---
