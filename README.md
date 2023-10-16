<a name="readme-top"></a>
# Dining Chatbot Concierge With AWS #


<!-- ABOUT THE PROJECT -->
## About The Project
This project implemented a dining concierge chatbot which will recommend proper restaurants at certain location with certain cuisines demanded by customers. The chatbot will receive the customer's dining intent and send emails to customers with recommended restaurants.

### Built With

This project mainly used services provided by AWS.

1. S3
2. API Gateway
3. Lambda Function
4. Lex
5. SQS
6. SES
7. DynamoDB
8. OpenSearch
9. SES

### Project Architechture Diagram
![Project Architechture Diagram](./images/Project_Architechture_Diagram.png)


<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- USAGE EXAMPLES -->
## Usage

When users open the chatbot, it will greet you and ask you about the place, cuisine type, number of people and date of your meal. By answering questions one by one, users' intent will be recorded by the chatbot. Later the chatbot will send 5 random restaurant recommendations to the customer's email address.

### Interaction Example

Chatbot Screenshot 1
![Chatbot Screenshot 1](./images/Chatbot_Screenshot_1.png)
Chatbot Screenshot 2
![Chatbot Screenshot 2](./images/Chatbot_Screenshot_2.png)

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- CONTACT -->
## Contact

Letian Jiang (Email: lj2397@nyu.edu)

Project Link: [Dining Concierge Chatbot](http://diningconcierge-chatbot.s3-website.us-east-2.amazonaws.com/)

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- ACKNOWLEDGMENTS -->
## Acknowledgments

Use this space to list resources you find helpful and would like to give credit to. I've included a few of my favorites to kick things off!

* [Yelp API](https://docs.developer.yelp.com/docs/fusion-intro)
* [AWS DOC SDK Examples](https://github.com/awsdocs/aws-doc-sdk-examples/tree/main)


<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
