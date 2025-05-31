# Deepthink-Pro

Deepthink-Pro is an AI-driven assistant designed to enhance productivity and creativity through a versatile set of tools. Built with Spring Boot, Ollama, and the Qwen Model (qwen2.5:14b), it delivers a seamless conversational experience across web and mobile platforms.

![ss0](https://github.com/user-attachments/assets/ced1c0dc-a126-4c5d-9e04-b88227cd4d80)

## Features

* **AI Chat Assistant**: Natural language conversations with the AI to answer questions, brainstorm ideas, and explain complex topics.
* **Task Management**: Add, view, and update tasks using simple chat commands; tasks are tracked with real-time updates.
* **Document Parser**: Parse and structure information from any uploaded document—resumes, reports, invoices, and more.
* **Website Generator**: Choose from pre-built templates (Portfolio, Blog, Ecommerce, Restaurant, Photography) and generate static sites with preview and download options.

    ### Other features
    
    * **Clear History**: Instantly clear your chat history with one click.
    * **Edit Prompt**: Update your previous prompts.
    * **Download Chat**: Save entire conversations to your device for future reference.
    * **Copy Content**: Quickly copy text or code snippets from the chat interface.
    * **Responsive Design**: Optimized layout for both desktop and mobile devices.

## Technologies Used

* **Backend**: Spring Boot
* **AI Model**: Qwen via Ollama
* **Frontend**: HTML, CSS, JavaScript (static files served from Spring Boot)
* **Template Engine**: Thymeleaf or similar for dynamic pages
* **Document Parsing**: Tesseract OCR and custom parsers
* **Database**: MySQL (For CRUD operation - Task Management)

## Installation and Setup

### Prerequisites

* Java 17+
* Maven
* Spring Boot
* Git
* Ollama CLI (for Qwen models)

### Steps to Set Up

1. **Clone the repository**

   ```bash
   git clone https://github.com/NeerajKumar805/deepthink-pro.git
   cd deepthink-pro
   ```

2. **Install and configure Ollama models**

   * Download and install Ollama from the official site.
   * Pull the required Qwen models:

    I'm mentioning the models that I've used in this project:

     ```bash
     ollama pull qwen2.5:1.5b
     ollama pull qwen2.5:3b
     ollama pull qwen2.5:7b
     ```

3. **Build and run the backend**

   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

   The backend starts on `http://localhost:9990` (adjust port in `application.properties` if needed).

4. **Integrate the frontend**

   * Place your compiled frontend files (HTML, CSS, JS) into `src/main/resources/static`.

5. **Test the API**

   * Use Postman or Swagger UI (`http://localhost:9990/swagger-ui.html`) to verify endpoints.

## Screenshots

Here are a few snapshots showcasing the interface and features:

![ss0](https://github.com/user-attachments/assets/ced1c0dc-a126-4c5d-9e04-b88227cd4d80)
![ss1](https://github.com/user-attachments/assets/5104d15a-26e0-42fb-95c7-0be986809b07)
![ss2](https://github.com/user-attachments/assets/3771e04c-ce7d-43d8-b2cd-53510ad9be1a)
![ss3](https://github.com/user-attachments/assets/e4fc6af5-4bcf-4cb6-9c97-8109e40a2e6e)
![ss4](https://github.com/user-attachments/assets/65efdfb6-469e-4209-9a03-4add105b30c3)
![ss5](https://github.com/user-attachments/assets/89a023d7-4cfe-409c-b688-c4f8eb1cba0b)
![ss6](https://github.com/user-attachments/assets/cff52b19-ee2c-453c-9b19-fa14586feedb)
![ss7](https://github.com/user-attachments/assets/fcaaf4f7-e57a-4c64-b555-bd2656d452b0)
![ss8](https://github.com/user-attachments/assets/c9b1e9f7-9956-40f9-ac24-653ab7400ae0)
![ss9](https://github.com/user-attachments/assets/2d116ca0-e7b7-42d9-9dab-a09a0efea669)
![ss10](https://github.com/user-attachments/assets/4fa2bb09-f9f2-4276-82f6-4992bb35037e)


## Usage

* Open your browser and navigate to `http://localhost:9990`.
* Interact with Deepthink-Pro through the chat window to access features.

## Contributing

Contributions, issues, and feature requests are welcome. Feel free to open a pull request or create an issue on GitHub.

## License

This project is under development and not yet licensed for commercial use. Contact the author for inquiries.

## Author

**Neeraj Kumar**
Email: [neerajkumarroy805@gmail.com](mailto:neerajkumarroy805@gmail.com)
Phone: +91 7260925071
GitHub: [NeerajKumar805](https://github.com/NeerajKumar805)
