// teacherData.js — shared mock data: students, courses, subjects, evaluations
window.TEACHER_DATA = (() => {
  const SUBJECTS_BY_COURSE = {
    "1°B": ["Biología", "Historia", "Matemática"],
    "2°A": ["Historia", "Lenguaje", "Matemática"],
    "3°A": ["Lenguaje", "Matemática", "Filosofía"],
    "2°B": ["Matemática", "Historia", "Ciencias"],
  };
  const COURSES = Object.keys(SUBJECTS_BY_COURSE);

  const EVAL_NAMES = {
    "Biología":   ["Control U1","Prueba Fotosíntesis","Trabajo Ecosistemas","Control U2","Prueba U2","Control U3","Trabajo Evolución","Prueba U3"],
    "Historia":   ["Control U1","Ensayo Renacimiento","Prueba U1","Control U2","Ensayo R. Industrial","Prueba U2"],
    "Matemática": ["Control U1","Prueba Funciones","Control U2","Prueba Derivadas","Control U3","Prueba Integrales","Control U4"],
    "Lenguaje":   ["Control U1","Ensayo Literario","Prueba Novela","Control U2","Análisis Poético"],
    "Filosofía":  ["Control U1","Ensayo Ética","Prueba U1","Control U2"],
    "Ciencias":   ["Control U1","Prueba Química","Trabajo Lab","Control U2","Prueba Física"],
  };
  const DATES = ["Mar 5","Mar 22","Abr 8","Abr 25","May 10","May 28","Jun 5","Jun 18"];

  const _avg  = a => a.reduce((s,x)=>s+x,0)/a.length;
  const _r1   = n => Math.round(n*10)/10;
  const _trend = a => {
    if(a.length<2) return "flat";
    const h=Math.floor(a.length/2), d=_avg(a.slice(h))-_avg(a.slice(0,h));
    return d>0.3?"up":d<-0.3?"down":"flat";
  };
  const _rubric = s => s>=6.0?"advanced":s>=5.0?"proficient":s>=4.0?"developing":"beginning";

  const RAW = [
    {id:"s01",name:"Antonia Bravo",    course:"1°B",email:"a.bravo@school.cl",   grades:{"Biología":[6.2,6.8,6.4,7.0,6.5,6.8,6.3,6.7],"Historia":[5.5,6.0,6.0,5.8,5.5],"Matemática":[5.0,5.5,5.2,5.8,5.5]}},
    {id:"s02",name:"Diego Soto",       course:"1°B",email:"d.soto@school.cl",    grades:{"Biología":[5.0,5.2,5.1,4.8,5.5,5.0,5.2,5.1],"Historia":[4.0,4.8,4.5,4.5,4.8],"Matemática":[4.0,4.5,4.2,4.0,4.2]}},
    {id:"s03",name:"Josefa Díaz",      course:"1°B",email:"j.diaz@school.cl",    grades:{"Biología":[3.8,4.2,4.1,4.0,4.2,4.0,3.9,4.1],"Historia":[3.2,3.8,3.5,3.4,3.5],"Matemática":[3.5,4.0,3.8,3.8,3.8]}},
    {id:"s04",name:"Camila Rojas",     course:"2°A",email:"c.rojas@school.cl",   grades:{"Historia":[6.5,6.8,6.5,6.7,6.5,6.5],"Lenguaje":[6.0,6.5,6.2,6.0,6.5],"Matemática":[5.5,6.0,5.8,5.8,6.0]}},
    {id:"s05",name:"Martín Cáceres",   course:"2°A",email:"m.caceres@school.cl", grades:{"Historia":[5.5,6.0,5.8,6.0,5.8,5.5],"Lenguaje":[5.0,5.5,5.2,5.2,5.0],"Matemática":[5.2,5.8,5.5,5.5,5.5]}},
    {id:"s06",name:"Isidora Guzmán",   course:"2°A",email:"i.guzman@school.cl",  grades:{"Historia":[3.5,4.0,3.8,3.8,3.5,4.0],"Lenguaje":[4.0,4.5,4.2,4.0,4.2],"Matemática":[3.2,3.8,3.5,3.5,3.2]}},
    {id:"s07",name:"Benjamín Flores",  course:"2°A",email:"b.flores@school.cl",  grades:{"Historia":[5.0,5.5,5.2,5.2,5.0,5.2],"Lenguaje":[4.5,5.0,4.8,5.0,4.8],"Matemática":[4.8,5.2,5.0,5.0,5.0]}},
    {id:"s08",name:"Valentina Mora",   course:"3°A",email:"v.mora@school.cl",    grades:{"Lenguaje":[6.0,6.5,6.0,6.0,6.0],"Matemática":[5.5,6.0,5.8,5.8,6.0],"Filosofía":[5.2,5.8,5.5,5.5]}},
    {id:"s09",name:"Tomás Vidal",      course:"3°A",email:"t.vidal@school.cl",   grades:{"Lenguaje":[3.0,3.5,3.2,3.0,3.5],"Matemática":[3.5,4.0,3.8,3.8,3.5],"Filosofía":[4.0,4.5,4.2,4.2]}},
    {id:"s10",name:"Sofía Herrera",    course:"3°A",email:"s.herrera@school.cl", grades:{"Lenguaje":[5.5,5.8,5.5,5.5,6.0],"Matemática":[4.8,5.2,5.0,5.0,5.2],"Filosofía":[5.5,5.8,5.8,5.5]}},
    {id:"s11",name:"Gabriel Torres",   course:"2°B",email:"g.torres@school.cl",  grades:{"Matemática":[4.0,4.5,3.8,4.2,4.0,4.2,3.9],"Historia":[5.0,5.5,5.2,5.0,5.2],"Ciencias":[5.2,5.8,5.5,5.5,5.0]}},
    {id:"s12",name:"Daniela Parra",    course:"2°B",email:"d.parra@school.cl",   grades:{"Matemática":[3.5,3.8,3.2,3.5,3.8,3.5,3.2],"Historia":[4.8,5.0,4.8,5.0,4.5],"Ciencias":[4.5,5.0,4.8,4.5,4.8]}},
    {id:"s13",name:"Rodrigo Núñez",    course:"2°B",email:"r.nunez@school.cl",   grades:{"Matemática":[5.5,5.8,6.0,5.5,5.8,5.5,6.0],"Historia":[5.5,5.8,5.5,5.8,5.5],"Ciencias":[6.0,6.2,6.0,6.0,5.8]}},
  ];

  const students = RAW.map(s => {
    const grades = {};
    for(const [sub, scores] of Object.entries(s.grades)){
      const a = _avg(scores);
      grades[sub] = { avg:_r1(a), scores, evals:scores.length, trend:_trend(scores), risk:a<4.0 };
    }
    const allAvgs = Object.values(grades).map(g=>g.avg);
    return { ...s, grades, avg:_r1(_avg(allAvgs)), trend:_trend(allAvgs), risk:Object.values(grades).some(g=>g.risk) };
  });

  // Returns individual evaluation rows for a student+subject
  const getEvals = (studentId, subject) => {
    const s = students.find(x=>x.id===studentId);
    if(!s || !s.grades[subject]) return [];
    return s.grades[subject].scores.map((score,i)=>({
      title: (EVAL_NAMES[subject]||[])[i] || `Evaluación ${i+1}`,
      date:  DATES[i] || "—",
      score,
      rubric: _rubric(score),
    }));
  };

  // Returns aggregate stats for a course-subject pair
  const subjectStats = (course, subject) => {
    const ss = students.filter(s=>s.course===course && s.grades[subject]);
    if(!ss.length) return null;
    const avgs = ss.map(s=>s.grades[subject].avg);
    const globalAvg = _r1(_avg(avgs));
    const maxScore  = _r1(Math.max(...avgs));
    const minScore  = _r1(Math.min(...avgs));
    const riskCount = ss.filter(s=>s.grades[subject].risk).length;
    const totalEvals= ss[0].grades[subject].evals;
    return { students:ss, count:ss.length, avg:globalAvg, max:maxScore, min:minScore, riskCount, totalEvals };
  };

  return { students, SUBJECTS_BY_COURSE, COURSES, getEvals, subjectStats, _avg, _r1 };
})();
