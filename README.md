

> `Tests` (compatibility matrix)

### 👉 Set Environment

1. Install Python >= 3.7

2. Install NPM
| `v16.20.0` 

3. Run powershell.exe as administator and Execute follow command
```bash
 Set-ExecutionPolicy RemoteSigned -Force
```
### 👉 Start the Frontend 

> **Step 1** - Once the project is downloaded, change the directory to `react-ui`. 

```bash
$ cd frontend
```

<br >

> **Step 2** - Install dependencies via NPM or yarn

```bash
$ npm i --legacy-peer-deps
// OR
$ yarn
```

<br />

> **Step 3** - Start in development mode

```bash
$ npm run start 
// OR
$ yarn start
```

<br />

At this point, the app is available in the browser `localhost:3000` (the default address).
<br /> 

### 👉 Start the Backend Server 

> **Step 1** - Change the directory to `backend`

```bash
$ cd backend
```

<br >

> **Step 2** - Install dependencies using a `virtual environment`


```bash
# (Windows based systems)
$ python -m venv env
$ .\env\Scripts\activate.ps1

$ pip install -r requirements.txt
```

<br />

> **Step 3** - Setup the database 

```bash
$ python manage.py makemigrations
$ python manage.py migrate
```
<br />

> **Step 4** - Start the API server (development mode)

```bash
$ python manage.py runserver 5000
```

Use the API via `POSTMAN` or `Swagger Dashboard` at `localhost:5000`.
<br />

