/**
 * SSD: 공유 장바구니 동시성 이슈 (문제 시나리오)
 *
 * 시나리오:
 *   고객 A가 장바구니 수정(순대 수량 3→1) 후,
 *   고객 B가 Polling 지연으로 인해 아직 수량 3으로 인식한 채 주문 → 실제 주문은 수량 1로 처리됨.
 *
 * Run:   node docs/order/diagrams/generate-ssd-shared-cart-concurrency.js
 * Output: C:/personal_study/Excalidraw/ssd-shared-cart-concurrency.excalidraw
 */
const fs = require('fs');

// ========== 참여자 아이콘 원본 JSON (절대 수정 금지) ==========
const ICON_SOURCE = {"type":"excalidraw/clipboard","elements":[{"type":"line","version":2547,"versionNonce":294557591,"isDeleted":false,"id":"hDaBkjeyL02yuG2htQoao","fillStyle":"hachure","strokeWidth":1,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":0,"x":-977.8893332215523,"y":941.7794468675756,"strokeColor":"#000000","backgroundColor":"#000000","width":132.47542224713416,"height":83.20760183833374,"seed":1201926169,"groupIds":["4M_HyJZH9XOdK5rX7lYsu","QliQRC6x8IGyp5t80g3RF","ErWk3B-7NeIcmJ6xYvtOd","Oj3wZjcGULlaIa5X1hROb"],"strokeSharpness":"round","startBinding":null,"endBinding":null,"points":[[0,0],[0.28799004836331515,17.436887149944926],[1.151960193453329,35.17968460076614],[16.127442708346763,49.25155844107256],[44.92644754468024,56.2874953612257],[86.6850045573638,56.28749536122576],[115.19601934533401,49.86337904282497],[130.60348693277248,37.779922158214006],[132.47542224713416,17.43688714994495],[130.3154968844091,-2.753192707886041],[112.89209895842716,-19.57825925607851],[82.65314388027714,-26.92010647710799],[44.63845749631691,-26.308285875355487],[14.831487490711739,-16.519156247316264],[0,0]],"lastCommittedPoint":null,"startArrowhead":null,"endArrowhead":null,"index":"b0L","frameId":null,"roundness":{"type":2},"boundElements":[],"updated":1774367675548,"link":null,"locked":false},{"type":"ellipse","version":1543,"versionNonce":489000119,"isDeleted":false,"id":"8AsDg-fj03nnawcLxruzk","fillStyle":"solid","strokeWidth":1,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":0,"x":-966.207873270316,"y":918.2110398280247,"strokeColor":"#000000","backgroundColor":"#ffff","width":107.98056659404219,"height":41.29568410820307,"seed":499156217,"groupIds":["4M_HyJZH9XOdK5rX7lYsu","QliQRC6x8IGyp5t80g3RF","ErWk3B-7NeIcmJ6xYvtOd","Oj3wZjcGULlaIa5X1hROb"],"strokeSharpness":"sharp","index":"b0M","frameId":null,"roundness":null,"boundElements":[],"updated":1774367675548,"link":null,"locked":false},{"type":"line","version":3403,"versionNonce":1045760759,"isDeleted":false,"id":"AenQpk2dEHgmwkyv8yhrf","fillStyle":"hachure","strokeWidth":1,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":0,"x":-977.3051820577532,"y":988.9703256834009,"strokeColor":"#000000","backgroundColor":"#000000","width":130.4259327395418,"height":53.76417943187906,"seed":825720281,"groupIds":["4M_HyJZH9XOdK5rX7lYsu","QliQRC6x8IGyp5t80g3RF","ErWk3B-7NeIcmJ6xYvtOd","Oj3wZjcGULlaIa5X1hROb"],"strokeSharpness":"round","startBinding":null,"endBinding":null,"points":[[0,0],[0.28799004836331527,16.717018287674158],[2.436714810152993,35.52307612135063],[18.696951941746093,47.417773361938266],[46.8535794697297,53.76417943187906],[87.54150763516353,53.56465070705557],[112.84063588138486,48.80245030641387],[127.66690495174473,38.55754278075031],[130.4259327395418,20.36554354158975],[129.9730526687116,1.6657210237169469],[126.09416028668178,4.115943337136098],[110.90378824210654,11.216331484809166],[85.681494048212,15.921277603671399],[48.920972885315784,16.393823234668073],[19.41989683606764,10.956682113540802],[0,0]],"lastCommittedPoint":null,"startArrowhead":null,"endArrowhead":null,"index":"b0N","frameId":null,"roundness":{"type":2},"boundElements":[],"updated":1774367675549,"link":null,"locked":false},{"type":"line","version":3459,"versionNonce":59151383,"isDeleted":false,"id":"KKCj0T2shqKuWIjafN-i3","fillStyle":"hachure","strokeWidth":1,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":0,"x":-977.2434639571004,"y":1034.0449980790045,"strokeColor":"#000000","backgroundColor":"#000000","width":130.4259327395418,"height":52.25320337216551,"seed":823941817,"groupIds":["4M_HyJZH9XOdK5rX7lYsu","QliQRC6x8IGyp5t80g3RF","ErWk3B-7NeIcmJ6xYvtOd","Oj3wZjcGULlaIa5X1hROb"],"strokeSharpness":"round","startBinding":null,"endBinding":null,"points":[[0,0],[0.28799004836331527,16.247207073416277],[2.436714810152993,34.524743808760455],[18.696951941746093,46.08515523009096],[46.8535794697297,52.25320337216551],[87.54150763516353,52.05928215646091],[112.84063588138486,47.43091753408195],[127.66690495174473,37.47393051922751],[130.4259327395418,19.793195017729463],[129.9730526687116,1.6189079854525645],[126.09416028668178,4.000269817866081],[110.90378824210654,10.901110299803792],[85.681494048212,15.473829701491836],[48.920972885315784,15.933095019404544],[19.41989683606764,10.648758054392303],[0,0]],"lastCommittedPoint":null,"startArrowhead":null,"endArrowhead":null,"index":"b0O","frameId":null,"roundness":{"type":2},"boundElements":[],"updated":1774367675549,"link":null,"locked":false},{"id":"h0rlMsR2","type":"text","x":-939.3794459037074,"y":1108.547500632717,"width":55.29301452636719,"height":29.935031674280964,"angle":0,"strokeColor":"#1e1e1e","backgroundColor":"transparent","fillStyle":"hachure","strokeWidth":1,"strokeStyle":"dashed","roughness":0,"opacity":100,"groupIds":["h5Mn7bgikTV_C5aqYlIKk","TuqSJWKVCPS36iuXaL9Tu","UhmrW29rlXzqlnMwZ2Qo9","QliQRC6x8IGyp5t80g3RF","ErWk3B-7NeIcmJ6xYvtOd","Oj3wZjcGULlaIa5X1hROb"],"frameId":null,"index":"b0P","roundness":null,"seed":504535961,"version":1151,"versionNonce":2011822391,"isDeleted":false,"boundElements":[],"updated":1774367675549,"link":null,"locked":false,"text":"RDB","fontSize":23.94802533942477,"fontFamily":5,"textAlign":"left","verticalAlign":"top","containerId":null,"originalText":"RDB","autoResize":true,"lineHeight":1.25,"rawText":"RDB"},{"type":"line","version":1663,"versionNonce":283359831,"isDeleted":false,"id":"dDox-sKdv8Yb_m5lMRaDP","fillStyle":"cross-hatch","strokeWidth":2,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":0,"x":-833.108444410773,"y":690.9049699214602,"strokeColor":"#fab005","backgroundColor":"#fab005","width":0,"height":0.1285389735648001,"seed":407748729,"groupIds":["QKUY31TLc3PtbtMqqsO_o","LKgK3lIc_hvdrQzyvdMAs","RbBau9gZKi_eU2uWsWNT-"],"strokeSharpness":"round","startBinding":null,"endBinding":null,"points":[[0,0],[0,0.1285389735648001]],"lastCommittedPoint":null,"startArrowhead":null,"endArrowhead":null,"index":"b0Q","frameId":null,"roundness":{"type":2},"boundElements":[],"updated":1774367675549,"link":null,"locked":false},{"type":"ellipse","version":1895,"versionNonce":591943543,"isDeleted":false,"id":"my9Wvc9l7C-7_CsM0k0cY","fillStyle":"cross-hatch","strokeWidth":2,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":6.099831520917273,"x":-993.4528726226129,"y":572.1563306746912,"strokeColor":"#bfc548","backgroundColor":"#bfc548","width":159.67592946819278,"height":157.9132911464215,"seed":1838238041,"groupIds":["QKUY31TLc3PtbtMqqsO_o","LKgK3lIc_hvdrQzyvdMAs","RbBau9gZKi_eU2uWsWNT-"],"strokeSharpness":"round","index":"b0R","frameId":null,"roundness":{"type":2},"boundElements":[],"updated":1774367675549,"link":null,"locked":false},{"type":"line","version":2892,"versionNonce":122860375,"isDeleted":false,"id":"cSw49J-BcHQDTKqWUT0iB","fillStyle":"solid","strokeWidth":2,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":6.099831520917273,"x":-963.1024801429633,"y":694.9132984188045,"strokeColor":"#bfc548","backgroundColor":"#ffffff","width":156.09731888630643,"height":106.49515332688762,"seed":1731245625,"groupIds":["QKUY31TLc3PtbtMqqsO_o","LKgK3lIc_hvdrQzyvdMAs","RbBau9gZKi_eU2uWsWNT-"],"strokeSharpness":"round","startBinding":null,"endBinding":null,"points":[[0,0],[-0.6634782883291491,0.6344070560217531],[-12.615471723798992,-18.417405281952075],[-13.541781775542917,-38.08226411054242],[-6.76016460707023,-51.82393558145009],[6.936119154026951,-62.989612144180946],[33.44731623080832,-69.8964447513349],[59.76130655391609,-67.63943985795657],[98.95280577226349,-61.050250620054214],[120.71193465375681,-66.70602332628124],[134.6003841052258,-77.64632246406714],[142.5555371107635,-92.707586217219],[138.1346659426124,-56.522970685204974],[127.36693440523374,-32.725370101570896],[105.26772446922746,-10.05543427183329],[65.34614252455196,11.828416509561835],[27.722433371592704,13.787567109668617],[0.49281983096364634,1.6604633660161154],[0,0]],"lastCommittedPoint":null,"startArrowhead":null,"endArrowhead":null,"index":"b0S","frameId":null,"roundness":{"type":2},"boundElements":[],"updated":1774367675549,"link":null,"locked":false},{"type":"line","version":4236,"versionNonce":1131425911,"isDeleted":false,"id":"dzWVjNBiiLmrN7V3SlnOj","fillStyle":"cross-hatch","strokeWidth":2,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":6.099831520917273,"x":-949.9915300520554,"y":688.4231658509825,"strokeColor":"#bfc548","backgroundColor":"#82c91e","width":118.65910187621134,"height":48.556162324596826,"seed":1477412633,"groupIds":["QKUY31TLc3PtbtMqqsO_o","LKgK3lIc_hvdrQzyvdMAs","RbBau9gZKi_eU2uWsWNT-"],"strokeSharpness":"round","startBinding":null,"endBinding":null,"points":[[0,0],[11.290688003818344,-0.9348977749984873],[25.789395333304526,-1.99178145492064],[49.95446077938781,-6.813506661268055],[74.33381244509464,-15.531290842060057],[86.35384539346633,-23.248365025742622],[92.5887857336727,-27.045477272698747],[97.37553642052602,-32.887162754832154],[101.47301163305866,-38.15468187317639],[101.36330975069814,-34.87777850438089],[93.3078099662197,-22.51662174536123],[90.72525133062783,-19.950960654196297],[88.69888190517905,-20.624133817777498],[82.18563522005186,-11.091563514475478],[67.0454586781633,-1.4464645274954653],[46.62108175268632,5.315552705263947],[28.288805866738155,9.151287363160172],[9.517752448847254,10.401480451420433],[-8.304414174013848,9.706759132210333],[-17.186090243152677,0.6691577131107117],[0,0]],"lastCommittedPoint":null,"startArrowhead":null,"endArrowhead":null,"index":"b0T","frameId":null,"roundness":{"type":2},"boundElements":[],"updated":1774367675549,"link":null,"locked":false},{"type":"ellipse","version":2109,"versionNonce":76374969,"isDeleted":false,"id":"2510PaQj7fU0eat4FItEF","fillStyle":"solid","strokeWidth":2,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":6.099831520917273,"x":-972.0454912226962,"y":699.4275632236613,"strokeColor":"#bfc548","backgroundColor":"#ffffff","width":11.978994230125652,"height":11.558739576067893,"seed":1788951545,"groupIds":["QKUY31TLc3PtbtMqqsO_o","LKgK3lIc_hvdrQzyvdMAs","RbBau9gZKi_eU2uWsWNT-"],"strokeSharpness":"round","index":"b0U","frameId":null,"roundness":{"type":2},"boundElements":[],"updated":1774367675579,"link":null,"locked":false},{"type":"ellipse","version":1461,"versionNonce":876929655,"isDeleted":false,"id":"_clientHitbox","fillStyle":"solid","strokeWidth":1,"strokeStyle":"solid","roughness":1,"opacity":0,"angle":0,"x":-1456.0362248476488,"y":566.4843152045278,"strokeColor":"#1e1e1e","backgroundColor":"transparent","width":169.2573231636715,"height":169.2573231636715,"seed":787043545,"groupIds":["gszrnSHEZI6vOXYKt_t4Q"],"frameId":null,"roundness":null,"boundElements":[],"updated":1774367675549,"link":null,"locked":false,"index":"b0V"},{"type":"line","version":3408,"versionNonce":424674839,"isDeleted":false,"id":"YCKD8mM1SOq4nIFwJg7n-","fillStyle":"solid","strokeWidth":1,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":0,"x":-1413.001688597364,"y":682.2249257896392,"strokeColor":"#1e1e1e","backgroundColor":"transparent","width":78.78535850250793,"height":70.15609915710723,"seed":1752912313,"groupIds":["dhQpDBsE3u6IXL9R2SPaN","gszrnSHEZI6vOXYKt_t4Q"],"frameId":null,"roundness":{"type":2},"boundElements":[],"updated":1774367675549,"link":null,"locked":false,"startBinding":null,"endBinding":null,"lastCommittedPoint":null,"startArrowhead":null,"endArrowhead":null,"points":[[0,0],[8.823960152280884,-46.48898136916746],[37.816972081203815,-70.15609915710723],[66.80998401012675,-51.56050660944021],[78.78535850250793,-4.827291605468445],[0,0]],"index":"b0W"},{"type":"ellipse","version":3149,"versionNonce":934619959,"isDeleted":false,"id":"36sI8oQENicmMI9PHBH04","fillStyle":"solid","strokeWidth":1,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":0,"x":-1393.0751336549476,"y":575.4818573253497,"strokeColor":"#1e1e1e","backgroundColor":"transparent","width":40.33810355328409,"height":35.29584060912371,"seed":477803161,"groupIds":["dhQpDBsE3u6IXL9R2SPaN","gszrnSHEZI6vOXYKt_t4Q"],"frameId":null,"roundness":null,"boundElements":[],"updated":1774367675549,"link":null,"locked":false,"index":"b0X"},{"type":"text","version":4391,"versionNonce":1417194583,"isDeleted":false,"id":"5rHD1hwW","fillStyle":"solid","strokeWidth":1,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":0,"x":-1412.4253689876753,"y":690.5028313362263,"strokeColor":"#1e1e1e","backgroundColor":"transparent","width":79.03857421875,"height":36.241264911153664,"seed":1603991417,"groupIds":["ZEC3kwSrMwJMNSbm6bPrZ","gszrnSHEZI6vOXYKt_t4Q"],"frameId":null,"roundness":null,"boundElements":[],"updated":1774367675549,"link":null,"locked":false,"fontSize":28.993011928922932,"fontFamily":1,"text":"Client","textAlign":"center","verticalAlign":"top","containerId":null,"originalText":"Client","lineHeight":1.25,"baseline":18,"index":"b0Y","autoResize":true,"rawText":"Client"},{"type":"text","version":3136,"versionNonce":1265568119,"isDeleted":false,"id":"TmKSCAH9","fillStyle":"solid","strokeWidth":1,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":0,"x":-401.9064838426423,"y":710.7416383638329,"strokeColor":"#1e1e1e","backgroundColor":"transparent","width":76.3399658203125,"height":25,"seed":1679794265,"groupIds":["REvzJdUsRMamvAViBn6Bh","AFRluRq7tZ6Qfqj3NS99e"],"frameId":null,"roundness":null,"boundElements":[],"updated":1774367675549,"link":null,"locked":false,"fontSize":20,"fontFamily":1,"text":"PG API","textAlign":"center","verticalAlign":"top","containerId":null,"originalText":"PG API","lineHeight":1.25,"baseline":18,"index":"b0Z","rawText":"PG API","autoResize":true},{"type":"rectangle","version":2507,"versionNonce":165462679,"isDeleted":false,"id":"G3zsI0YZM9e_xBTLvVk1Q","fillStyle":"solid","strokeWidth":1,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":0,"x":-398.1256412981204,"y":580.3314697195538,"strokeColor":"#1e1e1e","backgroundColor":"transparent","width":66.77828073127297,"height":124.65279069837582,"seed":1205611833,"groupIds":["G8v5fPvw7G9KZ3efDYDMq","REvzJdUsRMamvAViBn6Bh","AFRluRq7tZ6Qfqj3NS99e"],"frameId":null,"roundness":null,"boundElements":[],"updated":1774367675549,"link":null,"locked":false,"index":"b0a"},{"type":"line","version":2324,"versionNonce":909055479,"isDeleted":false,"id":"MR9ZSYZ7P632BbsUPhFeP","fillStyle":"solid","strokeWidth":1,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":0,"x":-389.5755257404892,"y":594.1212461977057,"strokeColor":"#1e1e1e","backgroundColor":"transparent","width":51.19668189397568,"height":4.0741323554055855e-13,"seed":573840921,"groupIds":["G8v5fPvw7G9KZ3efDYDMq","REvzJdUsRMamvAViBn6Bh","AFRluRq7tZ6Qfqj3NS99e"],"frameId":null,"roundness":{"type":2},"boundElements":[],"updated":1774367675549,"link":null,"locked":false,"startBinding":null,"endBinding":null,"lastCommittedPoint":null,"startArrowhead":null,"endArrowhead":null,"points":[[0,0],[51.19668189397568,4.0741323554055855e-13]],"index":"b0b"},{"type":"line","version":2356,"versionNonce":703020823,"isDeleted":false,"id":"317WS13Qt9n2HzQClYNrG","fillStyle":"solid","strokeWidth":1,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":0,"x":-391.2094933855153,"y":605.5957142390771,"strokeColor":"#1e1e1e","backgroundColor":"transparent","width":51.19668189397568,"height":4.0741323554055855e-13,"seed":1377497849,"groupIds":["G8v5fPvw7G9KZ3efDYDMq","REvzJdUsRMamvAViBn6Bh","AFRluRq7tZ6Qfqj3NS99e"],"frameId":null,"roundness":{"type":2},"boundElements":[],"updated":1774367675549,"link":null,"locked":false,"startBinding":null,"endBinding":null,"lastCommittedPoint":null,"startArrowhead":null,"endArrowhead":null,"points":[[0,0],[51.19668189397568,4.0741323554055855e-13]],"index":"b0c"},{"type":"line","version":2338,"versionNonce":50152503,"isDeleted":false,"id":"-qJjr04-iES4X0Q8bx5eR","fillStyle":"solid","strokeWidth":1,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":0,"x":-390.9833011199555,"y":617.0701822804522,"strokeColor":"#1e1e1e","backgroundColor":"transparent","width":51.19668189397568,"height":4.0741323554055855e-13,"seed":202648537,"groupIds":["G8v5fPvw7G9KZ3efDYDMq","REvzJdUsRMamvAViBn6Bh","AFRluRq7tZ6Qfqj3NS99e"],"frameId":null,"roundness":{"type":2},"boundElements":[],"updated":1774367675549,"link":null,"locked":false,"startBinding":null,"endBinding":null,"lastCommittedPoint":null,"startArrowhead":null,"endArrowhead":null,"points":[[0,0],[51.19668189397568,4.0741323554055855e-13]],"index":"b0d"},{"type":"line","version":2342,"versionNonce":290825559,"isDeleted":false,"id":"XdHlohh99VJ2as9GVC_ic","fillStyle":"solid","strokeWidth":1,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":0,"x":-391.1365653062169,"y":628.5446503218272,"strokeColor":"#1e1e1e","backgroundColor":"transparent","width":51.19668189397568,"height":4.0741323554055855e-13,"seed":674449593,"groupIds":["G8v5fPvw7G9KZ3efDYDMq","REvzJdUsRMamvAViBn6Bh","AFRluRq7tZ6Qfqj3NS99e"],"frameId":null,"roundness":{"type":2},"boundElements":[],"updated":1774367675549,"link":null,"locked":false,"startBinding":null,"endBinding":null,"lastCommittedPoint":null,"startArrowhead":null,"endArrowhead":null,"points":[[0,0],[51.19668189397568,4.0741323554055855e-13]],"index":"b0e"},{"type":"ellipse","version":1461,"versionNonce":876929655,"isDeleted":false,"id":"7dXjqQC6m7v6zu45RePtF","fillStyle":"solid","strokeWidth":1,"strokeStyle":"solid","roughness":1,"opacity":100,"angle":0,"x":-352.5394413845645,"y":640.9588000332233,"strokeColor":"#1e1e1e","backgroundColor":"transparent","width":11.89978484740311,"height":8.654388979929259,"seed":807127449,"groupIds":["6oqhFVtWwRJ329lHAXGGh","UthFLwTfkcmgT2hBBPFRS","G8v5fPvw7G9KZ3efDYDMq","REvzJdUsRMamvAViBn6Bh","AFRluRq7tZ6Qfqj3NS99e"],"frameId":null,"roundness":null,"boundElements":[],"updated":1774367675549,"link":null,"locked":false,"index":"b0f"}],"files":{}};

// ========== RDB 이동 대상 ID ==========
const RDB_DY = 300;
const RDB_TOP_Y = 918 + RDB_DY; // 1218

const RDB_IDS = new Set([
  'hDaBkjeyL02yuG2htQoao', '8AsDg-fj03nnawcLxruzk',
  'AenQpk2dEHgmwkyv8yhrf', 'KKCj0T2shqKuWIjafN-i3', 'h0rlMsR2',
]);

// ========== 참여자 아이콘 정리 ==========
// - "Client"  → "고객 A"
// - "PG API"  → "고객 B" (서버랙 아이콘 재사용)
// - RDB 아이콘은 RDB_DY 만큼 아래로 이동
const participantElements = ICON_SOURCE.elements
  .filter(el => el.id !== '_clientHitbox') // 투명 hitbox 제거
  .map(el => {
    if (RDB_IDS.has(el.id)) {
      return { ...el, y: el.y + RDB_DY, boundElements: [] };
    }
    if (el.id === '5rHD1hwW') {
      return { ...el, text: '고객 A', rawText: '고객 A', originalText: '고객 A', boundElements: [] };
    }
    if (el.id === 'TmKSCAH9') {
      return { ...el, text: '고객 B', rawText: '고객 B', originalText: '고객 B', boundElements: [] };
    }
    return { ...el, boundElements: [] };
  });

// ========== 유틸 ==========
let idxCounter = 0;
function nextIndex() {
  const hi = Math.floor(idxCounter / 26);
  const lo = idxCounter % 26;
  idxCounter++;
  return 'c0' + String.fromCharCode(65 + hi) + lo.toString(16);
}
function uid() {
  return Math.random().toString(36).substr(2, 10) + Math.random().toString(36).substr(2, 4);
}

/**
 * 화살표 + 레이블 텍스트 쌍 생성
 * @param {number} x       시작 x
 * @param {number} y       시작 y
 * @param {number} dx      수평 벡터 (+오른쪽 / -왼쪽)
 * @param {number} dy      수직 벡터 (+아래 / -위)
 * @param {string} label   레이블 (\n 줄바꿈)
 * @param {boolean} isError true → 빨간 점선
 */
function makeArrow(x, y, dx, dy, label, isError = false) {
  const arrowId = uid();
  const textId  = uid();
  const color   = isError ? '#e03131' : '#1e1e1e';
  const isVertical = dx === 0;

  const lines  = label.split('\n');
  const textH  = lines.length * 25;
  const textW  = Math.max(...lines.map(l => l.length)) * 11;
  const textX  = isVertical ? x + 12 : x + dx / 2 - textW / 2;
  const textY  = isVertical
    ? y + dy / 2 - textH / 2
    : (isError ? y + 6 : y - textH - 4);

  return [
    {
      type: 'arrow', id: arrowId,
      version: 1, versionNonce: Math.floor(Math.random() * 2e6),
      isDeleted: false, frameId: null, index: nextIndex(),
      roundness: { type: 2 }, seed: Math.floor(Math.random() * 2e6),
      boundElements: [{ type: 'text', id: textId }],
      updated: Date.now(), link: null, locked: false, groupIds: [],
      x, y, angle: 0,
      width: Math.abs(dx), height: Math.abs(dy),
      strokeColor: color, backgroundColor: 'transparent',
      fillStyle: 'solid', strokeWidth: 4,
      strokeStyle: isError ? 'dashed' : 'solid',
      roughness: 0, opacity: 100,
      points: [[0, 0], [dx, dy]],
      lastCommittedPoint: [dx, dy],
      startBinding: null, endBinding: null,
      startArrowhead: null, endArrowhead: 'arrow',
      elbowed: false,
    },
    {
      type: 'text', id: textId,
      version: 1, versionNonce: Math.floor(Math.random() * 2e6),
      isDeleted: false, frameId: null, index: nextIndex(),
      roundness: null, seed: Math.floor(Math.random() * 2e6),
      boundElements: [], updated: Date.now(), link: null, locked: false, groupIds: [],
      x: textX, y: textY, angle: 0,
      width: textW, height: textH,
      strokeColor: color, backgroundColor: 'transparent',
      fillStyle: 'solid', strokeWidth: 1, strokeStyle: 'solid',
      roughness: 0, opacity: 100,
      text: label, rawText: label, originalText: label,
      fontSize: 18, fontFamily: 5,
      textAlign: 'center', verticalAlign: 'middle',
      containerId: arrowId,
      autoResize: true, lineHeight: 1.25,
    },
  ];
}

// ========== 화살표 정의 ==========
//
// 참여자 중심 X:
//   고객 A  (스틱피겨) : -1371
//   우리서비스 (글로브) : -912
//   고객 B  (서버랙)   : -364
//   RDB (실린더)       : -912 (서비스와 동일 X, RDB_TOP_Y 아래)
//
// 수평 연결점:
//   고객 A → 서비스 : x=-1278, dx=+273
//   서비스 → 고객 A : x=-994,  dx=-288
//   고객 B → 서비스 : x=-402,  dx=-429
//   서비스 → 고객 B : x=-822,  dx=+416
//
// 수직 연결점 (서비스 ↔ RDB):
//   서비스 → RDB (↓) : x=-954, dy = RDB_TOP_Y - startY  (양수)
//   RDB → 서비스 (↑) : x=-874, y=RDB_TOP_Y, dy = targetY - RDB_TOP_Y  (음수)

const arrows = [
  // ── 고객 A: 장바구니 수정 ──────────────────────────────────
  ...makeArrow(-1278, 660, 273, 0, '1. 장바구니 수정\n(순대 수량 3→1)'),

  // 서비스 → RDB: UPDATE
  ...makeArrow(-954, 730, 0, RDB_TOP_Y - 730, '2. UPDATE carts\n(quantity=1)'),

  // RDB → 서비스: OK
  ...makeArrow(-874, RDB_TOP_Y, 0, 800 - RDB_TOP_Y, '3. OK'),

  // 서비스 → 고객 A: 수정 완료
  ...makeArrow(-994, 860, -288, 0, '4. 수정 완료 응답'),

  // ── 고객 B: Polling 지연 상태에서 주문 ────────────────────
  // (고객 B 화면에는 아직 수량 3이 표시 중 — 별도 노트로 표현)

  // 고객 B → 서비스: 주문하기 요청
  ...makeArrow(-402, 970, -429, 0, '5. 주문하기 요청\n(화면 인식: 순대 3개)'),

  // 서비스 → RDB: SELECT
  ...makeArrow(-954, 1040, 0, RDB_TOP_Y - 1040, '6. SELECT cart\n(receiptId)'),

  // RDB → 서비스: 실제 cart 반환
  ...makeArrow(-874, RDB_TOP_Y, 0, 1110 - RDB_TOP_Y, '7. cart 반환\n(순대 수량=1)'),

  // 서비스 → 고객 B: 주문 완료 (데이터 불일치 — 에러 강조)
  ...makeArrow(-822, 1170, 416, 0, '8. 주문 완료!\n(순대 1개로 처리됨)', true),
];

// ========== 주석 노트: 고객 B 화면 상태 ==========
const NOTE_TEXT = '⚠ 고객 B 화면:\n순대 3개로 인식 중\n(Polling 지연)';
const noteLines = NOTE_TEXT.split('\n');
const noteW = Math.max(...noteLines.map(l => l.length)) * 10 + 24;
const noteH = noteLines.length * 22 + 16;
const noteRectId = uid();
const noteLabelId = uid();
const noteElements = [
  {
    type: 'rectangle', id: noteRectId,
    version: 1, versionNonce: Math.floor(Math.random() * 2e6),
    isDeleted: false, frameId: null, index: nextIndex(),
    roundness: { type: 3 }, seed: Math.floor(Math.random() * 2e6),
    boundElements: [], updated: Date.now(), link: null, locked: false, groupIds: [],
    x: -165, y: 890, angle: 0,
    width: noteW, height: noteH,
    strokeColor: '#f08c00', backgroundColor: '#fff3cd',
    fillStyle: 'solid', strokeWidth: 1, strokeStyle: 'dotted',
    roughness: 0, opacity: 100,
  },
  {
    type: 'text', id: noteLabelId,
    version: 1, versionNonce: Math.floor(Math.random() * 2e6),
    isDeleted: false, frameId: null, index: nextIndex(),
    roundness: null, seed: Math.floor(Math.random() * 2e6),
    boundElements: [], updated: Date.now(), link: null, locked: false, groupIds: [],
    x: -157, y: 898, angle: 0,
    width: noteW - 16, height: noteH,
    strokeColor: '#f08c00', backgroundColor: 'transparent',
    fillStyle: 'solid', strokeWidth: 1, strokeStyle: 'solid',
    roughness: 0, opacity: 100,
    text: NOTE_TEXT, rawText: NOTE_TEXT, originalText: NOTE_TEXT,
    fontSize: 16, fontFamily: 5,
    textAlign: 'left', verticalAlign: 'top',
    containerId: null, autoResize: true, lineHeight: 1.25,
  },
];

// ========== 서비스 라벨 (글로브 아이콘에 별도 라벨 없음) ==========
const serviceLabel = {
  type: 'text', id: uid(),
  version: 1, versionNonce: Math.floor(Math.random() * 2e6),
  isDeleted: false, frameId: null, index: nextIndex(),
  roundness: null, seed: Math.floor(Math.random() * 2e6),
  boundElements: [], updated: Date.now(), link: null, locked: false, groupIds: [],
  x: -1005, y: 726, angle: 0,
  width: 100, height: 25,
  strokeColor: '#1e1e1e', backgroundColor: 'transparent',
  fillStyle: 'solid', strokeWidth: 1, strokeStyle: 'solid',
  roughness: 0, opacity: 100,
  text: '우리 서비스', rawText: '우리 서비스', originalText: '우리 서비스',
  fontSize: 18, fontFamily: 5,
  textAlign: 'center', verticalAlign: 'top',
  containerId: null, autoResize: true, lineHeight: 1.25,
};

// ========== 문제 요약 텍스트 ==========
const SUMMARY_TEXT = '문제: 고객 B가 주문 완료를 받았지만\n실제 메뉴 수량이 기대와 다름 → 컴플레인 발생';
const summaryId = uid();
const summaryLines = SUMMARY_TEXT.split('\n');
const sumW = Math.max(...summaryLines.map(l => l.length)) * 10 + 24;
const sumH = summaryLines.length * 22 + 16;
const summaryElements = [
  {
    type: 'rectangle', id: summaryId,
    version: 1, versionNonce: Math.floor(Math.random() * 2e6),
    isDeleted: false, frameId: null, index: nextIndex(),
    roundness: { type: 3 }, seed: Math.floor(Math.random() * 2e6),
    boundElements: [], updated: Date.now(), link: null, locked: false, groupIds: [],
    x: -1490, y: 1210, angle: 0,
    width: sumW, height: sumH,
    strokeColor: '#e03131', backgroundColor: '#ffd0d0',
    fillStyle: 'solid', strokeWidth: 2, strokeStyle: 'solid',
    roughness: 0, opacity: 100,
  },
  {
    type: 'text', id: uid(),
    version: 1, versionNonce: Math.floor(Math.random() * 2e6),
    isDeleted: false, frameId: null, index: nextIndex(),
    roundness: null, seed: Math.floor(Math.random() * 2e6),
    boundElements: [], updated: Date.now(), link: null, locked: false, groupIds: [],
    x: -1482, y: 1218, angle: 0,
    width: sumW - 16, height: sumH,
    strokeColor: '#e03131', backgroundColor: 'transparent',
    fillStyle: 'solid', strokeWidth: 1, strokeStyle: 'solid',
    roughness: 0, opacity: 100,
    text: SUMMARY_TEXT, rawText: SUMMARY_TEXT, originalText: SUMMARY_TEXT,
    fontSize: 16, fontFamily: 5,
    textAlign: 'left', verticalAlign: 'top',
    containerId: null, autoResize: true, lineHeight: 1.25,
  },
];

// ========== 경계박스 + 제목 ==========
const DIAGRAM_TITLE = '공유 장바구니 동시성 이슈 — Polling 지연으로 인한 주문 데이터 불일치 (문제 시나리오)';
const FRAME_COLOR   = '#e03131';
const FRAME_HEIGHT  = 750;

const frameElements = [
  {
    type: 'rectangle', id: uid(),
    version: 1, versionNonce: Math.floor(Math.random() * 2e6),
    isDeleted: false, frameId: null, index: nextIndex(),
    roundness: { type: 3 }, seed: Math.floor(Math.random() * 2e6),
    boundElements: [], updated: Date.now(), link: null, locked: false, groupIds: [],
    x: -1498.421, y: 490, angle: 0,
    width: 1386.147, height: FRAME_HEIGHT,
    strokeColor: FRAME_COLOR, backgroundColor: 'transparent',
    fillStyle: 'solid', strokeWidth: 1, strokeStyle: 'dashed',
    roughness: 0, opacity: 100,
  },
  {
    type: 'text', id: uid(),
    version: 1, versionNonce: Math.floor(Math.random() * 2e6),
    isDeleted: false, frameId: null, index: nextIndex(),
    roundness: null, seed: Math.floor(Math.random() * 2e6),
    boundElements: [], updated: Date.now(), link: null, locked: false, groupIds: [],
    x: -1487.813, y: 450, angle: 0,
    width: 800, height: 30,
    strokeColor: FRAME_COLOR, backgroundColor: 'transparent',
    fillStyle: 'solid', strokeWidth: 1, strokeStyle: 'solid',
    roughness: 0, opacity: 100,
    text: DIAGRAM_TITLE, rawText: DIAGRAM_TITLE, originalText: DIAGRAM_TITLE,
    fontSize: 20, fontFamily: 5,
    textAlign: 'left', verticalAlign: 'top',
    containerId: null, autoResize: true, lineHeight: 1.25,
  },
];

// ========== 조립 & 출력 ==========
const output = {
  type: 'excalidraw', version: 2,
  source: 'https://excalidraw.com',
  elements: [
    ...participantElements,
    ...arrows,
    ...noteElements,
    serviceLabel,
    ...summaryElements,
    ...frameElements,
  ],
  appState: { gridSize: null, viewBackgroundColor: '#ffffff' },
  files: {},
};

const OUT = 'C:/personal_study/Excalidraw/ssd-shared-cart-concurrency.excalidraw';
fs.mkdirSync('C:/personal_study/Excalidraw', { recursive: true });
fs.writeFileSync(OUT, JSON.stringify(output, null, 2));
console.log('Generated:', OUT);
console.log('Elements :', output.elements.length);
