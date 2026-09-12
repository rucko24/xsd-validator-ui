# Xsd-validator-UI

<img width="1859" height="983" alt="image" src="https://github.com/user-attachments/assets/8513b057-c402-4dfe-a693-55ff2e33d5a9" />

## Analyze a file using the Monaco Editor

sample files to upload.

- [Folder validation-failure files](https://github.com/rucko24/xsd-validator-ui/tree/main/src/test/resources/documents/validation-failure)
- [Folder validation-successfully files](https://github.com/rucko24/xsd-validator-ui/tree/main/src/test/resources/documents/validation-successfully)

- Select these two files and validate them. `order-instance.xml` and `main-order.xsd`

<img width="972" height="144" alt="image" src="https://github.com/user-attachments/assets/398762b5-9d87-4c8d-8088-998b0ecd9c7d" />

<img width="1859" height="937" alt="image" src="https://github.com/user-attachments/assets/9c98887d-f3e7-4a1e-b602-edb82d1f97ff" />

## Starting in Development Mode

To start the application in development mode, import it into your IDE and run the `Application` class. 
You can also start the application from the command line by running: 

```bash
./mvnw
```

## Building for Production

To build the application in production mode, run:

```bash
./mvnw package
```

To build a Docker image, run:

```bash
docker build -t my-application:latest .
```

If you use commercial components, pass the license key as a build secret:

```bash
docker build --secret id=proKey,src=$HOME/.vaadin/proKey .
```

## Next Steps

The [Building Apps](https://vaadin.com/docs/v25/building-apps) guides contain hands-on advice for adding features to 
your application.
