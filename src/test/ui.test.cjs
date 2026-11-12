const {test} = require('node:test');
const assert = require('node:assert/strict');
const vm = require('node:vm');
const fs = require('node:fs');
const path = require('node:path');
const source = fs.readFileSync(path.join(__dirname,'../main/resources/static/app.js'),'utf8')
    .split("try{const session=await api('session')")[0];
function setup() {
    const nodes = new Map();
    const element = () => ({innerHTML:'',open:false,addEventListener(){},setAttribute(){},
        showModal(){this.open=true},querySelector(){return null},focus(){},classList:{toggle(){}}});
    const context = vm.createContext({console,Intl,Date,URL,crypto:require('node:crypto').webcrypto,
        localStorage:{getItem(){return null}},setInterval(){},setTimeout(){},
        window:{location:{hash:'#walkin',pathname:'/'},history:{replaceState(){}},scrollTo(){}},
        document:{querySelector(selector){
            if(selector==='#booking-form')return null;
            if(!nodes.has(selector))nodes.set(selector,element());
            return nodes.get(selector);
        },addEventListener(){},documentElement:{},body:element(),activeElement:element()}
    });
    vm.runInContext(source,context);
    vm.runInContext(`S.rooms=[{id:'MS-101',name:'Inspire',name_th:'ห้อง',location:'Floor 1',location_th:'ชั้น 1',capacity:6,active:1,approval:0,image:'/images/inspire-room.png',amenities:'wifi',color:'indigo'}]`,context);
    return {run:code=>vm.runInContext(code,context),nodes};
}
test('walk-in is a separate public screen with room photos',()=>{
    const {run,nodes}=setup();run('render()');
    const html=nodes.get('#app').innerHTML;
    assert.match(html,/Walk-in booking/);
    assert.match(html,/inspire-room.png/);
    assert.doesNotMatch(html,/data-action="showLogin"|data-filter="status"|data-filter="capacity"/);
});
test('guest booking form omits participants and repeat without redirecting to login',()=>{
    const {run,nodes}=setup();run("openBooking('MS-101')");
    const html=nodes.get('#dialog').innerHTML;
    assert.match(html,/booking-form/);
    assert.doesNotMatch(html,/name="participants"|name="repeat"|name="skip"/);
    assert.equal(run('S.authScreen'),false);
});
test('review omits participants and status and remains confirmable',()=>{
    const {run,nodes}=setup();
    run("openBooking('MS-101');draft.title='Study';draft.booker='Employee 123';reviewBooking()");
    const html=nodes.get('#dialog').innerHTML;
    assert.match(html,/confirm-form/);
    assert.doesNotMatch(html,/Participants|Status|Pending approval/);
});
test('admin has room image selection and upload, without approval or all-reservations panel',()=>{
    const {run,nodes}=setup();run("S.user={id:'admin',name:'Admin',role:'admin'};S.view='admin';render()");
    assert.doesNotMatch(nodes.get('#app').innerHTML,/All reservations|Pending approvals|Reservation requests|<th>Status/);
    run("roomForm('MS-101')");
    const html=nodes.get('#dialog').innerHTML;
    assert.match(html,/type="file"/);
    assert.match(html,/name="image"/);
    assert.doesNotMatch(html,/name="approval"/);
});
test('reservation lists show status and admin bookings show member or walk-in source',()=>{
    const {run,nodes}=setup();
    run("S.view='my';S.user={id:'u1',name:'Member',role:'user'};S.bookings=[{id:'b1',rid:'MS-101',uid:'u1',title:'Room reservation',date:'2999-01-01',start:'10:00',end:'11:00',status:'confirmed'}];render()");
    assert.match(nodes.get('#app').innerHTML,/badge confirmed/);
    run("S.view='admin';S.tab='bookingManagement';S.user={id:'admin',name:'Admin',role:'admin'};S.bookings=[{id:'b2',rid:'MS-101',uid:'walk-in',booker:'Employee 7',source:'Walk-in',title:'Room reservation',date:'2999-01-01',start:'10:00',end:'11:00',status:'cancelled'}];render()");
    const html=nodes.get('#app').innerHTML;
    assert.match(html,/Employee 7 · Walk-in booking/);
    assert.match(html,/badge cancelled/);
    assert.doesNotMatch(html,/<th>Status/);
});
test('keep and cancel buttons inside dialogs never submit the form',()=>{
    const {run,nodes}=setup();
    run("S.view='my';S.user={id:'u1',name:'Member',role:'user'};S.bookings=[{id:'b1',rid:'MS-101',uid:'u1',title:'Room reservation',date:'2999-01-01',start:'10:00',end:'11:00',status:'confirmed'}];cancelDialog('b1')");
    const html=nodes.get('#dialog').innerHTML;
    assert.match(html,/<button type="button" class="btn " data-action="close"\s*><span>Keep reservation/);
    assert.equal((html.match(/type="submit"/g)||[]).length,1);
    run("S.user={id:'admin',name:'Admin',role:'admin'};roomForm('MS-101')");
    assert.match(nodes.get('#dialog').innerHTML,/<button type="button" class="btn " data-action="close"\s*>/);
});
test('statuses are only confirmed or cancelled, with no approval actions',()=>{
    const {run,nodes}=setup();
    run("S.view='admin';S.tab='bookingManagement';S.user={id:'admin',name:'Admin',role:'admin'};S.bookings=[{id:'p1',rid:'MS-101',uid:'u1',title:'Room reservation',date:'2999-01-01',start:'10:00',end:'11:00',status:'pending'},{id:'r1',rid:'MS-101',uid:'u1',title:'Room reservation',date:'2999-01-02',start:'10:00',end:'11:00',status:'rejected'},{id:'e1',rid:'MS-101',uid:'u1',title:'Room reservation',date:'2000-01-01',start:'10:00',end:'11:00',status:'confirmed'}];render()");
    const html=nodes.get('#app').innerHTML;
    assert.doesNotMatch(html,/badge (pending|rejected|ended)|Pending|Approve|data-action="reject"/);
    assert.equal((html.match(/badge confirmed/g)||[]).length,2);
    assert.equal((html.match(/badge cancelled/g)||[]).length,1);
});
test('admin can delete a room; deleted rooms disappear from lists but keep history names',()=>{
    const {run,nodes}=setup();
    run("S.user={id:'admin',name:'Admin',role:'admin'};S.view='admin';S.tab='roomManagement';applyData({rooms:[S.rooms[0],{...S.rooms[0],id:'OLD-1',name:'Old Room',deleted:1}],bookings:[{id:'h1',rid:'OLD-1',uid:'admin',title:'Room reservation',date:'2000-01-01',start:'10:00',end:'11:00',status:'confirmed'}]});render()");
    let html=nodes.get('#app').innerHTML;
    assert.match(html,/data-action="deleteRoom" data-id="MS-101"/);
    assert.doesNotMatch(html,/Old Room/);
    run("deleteRoomDialog('MS-101')");
    const dialog=nodes.get('#dialog').innerHTML;
    assert.match(dialog,/delete-room-form/);
    assert.match(dialog,/<button type="button" class="btn " data-action="close"\s*><span>Keep room/);
    run("S.view='rooms';render()");
    assert.doesNotMatch(nodes.get('#app').innerHTML,/Old Room/);
    run("S.view='history';render()");
    assert.match(nodes.get('#app').innerHTML,/Old Room/);
});
test('admin bookings are split into walk-in and member sides and filtered by date and room',()=>{
    const {run,nodes}=setup();
    run(`S.user={id:'admin',name:'Admin',role:'admin'};S.view='admin';S.tab='bookingManagement';
      applyData({rooms:[S.rooms[0],{...S.rooms[0],id:'MS-204',name:'Studio'}],bookings:[
        {id:'w1',rid:'MS-101',uid:'walk-in',source:'Walk-in',booker:'Kiosk Kim',title:'Room reservation',date:'2999-01-01',start:'10:00',end:'11:00',status:'confirmed'},
        {id:'m1',rid:'MS-204',uid:'u1',source:'Member',booker:'Member May',title:'Room reservation',date:'2999-01-01',start:'09:00',end:'10:00',status:'confirmed'},
        {id:'m2',rid:'MS-101',uid:'u2',source:'Member',booker:'Member Max',title:'Room reservation',date:'2999-01-02',start:'09:00',end:'10:00',status:'confirmed'}]});render()`);
    const sides=()=>nodes.get('#app').innerHTML.split('Member bookings');
    let [walk,member]=sides();
    assert.match(walk,/Walk-in bookings[\s\S]*Kiosk Kim/);
    assert.doesNotMatch(walk,/Member May|Member Max/);
    assert.match(member,/Member May[\s\S]*Member Max/);
    assert.doesNotMatch(member,/Kiosk Kim/);
    assert.match(nodes.get('#app').innerHTML,/type="date" data-bfilter="date"/);
    run("S.bookingFilter={date:'2999-01-01',room:''};render()");
    [walk,member]=sides();
    assert.match(member,/Member May/);assert.doesNotMatch(member,/Member Max/);
    run("S.bookingFilter={date:'2999-01-01',room:'MS-101'};render()");
    [walk,member]=sides();
    assert.match(walk,/Kiosk Kim/);assert.doesNotMatch(member,/Member May/);
    assert.match(member,/No reservations match/);
});
test('anonymous occupied slots never appear as guest personal reservations',()=>{
    const {run}=setup();run("S.bookings=[{rid:'MS-101',status:'confirmed'}]");
    assert.equal(run('mine().length'),0);
});
