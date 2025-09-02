### Shake v4

---

I appen använder jag både accelerometer och ljussensor.
Accelerometern hämtar värdena ax, ay och az och genom att jämföra dessa mot jordens gravitation går det att se när telefonen skakas.
När skakningen blir större än en vald threshold roterar bilden (mariosvamp) 30 grader och en toast pop-up visar ”Shake!”.
Thresholden ändras via en drop down meny (spinner) där man kan välja känslighetsnivå.
Ljussensorn påverkar färgen på en ToggleButton, som går att slå av och på för att styra om ljussensorn ska användas.
Det finns även en reset-knapp som nollställer svampens position. Alla sensorvärden visas i en TextView. Appen har en custom ikon som även den är en mariosvamp.

---

Oliver Kalthoff, Java24
