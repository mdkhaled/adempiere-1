/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * Copyright (C) 2003-2014 E.R.P. Consultores y Asociados, C.A.               *
 * All Rights Reserved.                                                       *
 * Contributor(s): Raul Muñoz www.erpcya.com					              *
 *****************************************************************************/

package org.compiere.pos;

import java.awt.Color;
import java.awt.Event;
import java.awt.event.KeyEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import javax.swing.KeyStroke;

import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Borderlayout;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ListModelTable;
import org.adempiere.webui.component.ListboxFactory;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.Textbox;
import org.adempiere.webui.component.WListbox;
import org.adempiere.webui.event.TableValueChangeEvent;
import org.adempiere.webui.event.TableValueChangeListener;
import org.adempiere.webui.event.WTableModelEvent;
import org.adempiere.webui.event.WTableModelListener;
import org.adempiere.webui.window.FDialog;
import org.compiere.minigrid.ColumnInfo;
import org.compiere.minigrid.IDColumn;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerInfo;
import org.compiere.model.MCurrency;
import org.compiere.model.MImage;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPOS;
import org.compiere.model.MPOSKey;
import org.compiere.model.MPOSKeyLayout;
import org.compiere.model.MPriceList;
import org.compiere.model.MPriceListVersion;
import org.compiere.model.MProduct;
import org.compiere.model.MSequence;
import org.compiere.model.MSysConfig;
import org.compiere.model.MUser;
import org.compiere.model.MWarehousePrice;
import org.compiere.model.PO;
import org.compiere.print.MPrintColor;
import org.compiere.print.ReportCtl;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.zkoss.image.AImage;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zkex.zul.Center;
import org.zkoss.zkex.zul.East;
import org.zkoss.zkex.zul.North;
import org.zkoss.zkex.zul.South;
import org.zkoss.zkex.zul.West;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Space;


/**
 *	Customer Sub Panel
 *	
 * @author Raul Muñoz 20/03/2015 
 */
public class WSubOrder extends WPosSubPanel 
	implements EventListener, WTableModelListener, TableValueChangeListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5895558315889871887L;

	/**
	 * 	Constructor
	 *	@param posPanel POS Panel
	 */
	public WSubOrder (WPosBasePanel posPanel)
	{
		super (posPanel);
	}	//	PosSubCustomer
	
	private Button 		f_history;
	private	Textbox		f_name;
	private Button 		f_bNew;
	private Button 		f_cashPayment;

	private Button 		f_process;
	private Button 		f_print;
	private Label	 	f_DocumentNo;
	private Button 		f_logout;
	private Label	 	f_net;
	private Label	 	f_tax;
	private Label	 	f_total;
	private Label 		f_RepName;
	private Doublebox	f_discount;
	private Button 			f_Up;
	private Button 			f_Down;
	private Button 			f_Next;
	private Button 			f_Back;
	
	/**	The Business Partner		*/
	private MBPartner	m_bpartner;
	private Textbox f_currency = new Textbox();
	private Button f_bEdit;
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(SubOrder.class);
	
	
	private Button 			f_delete;
	//
	private Double	 		f_price;
	private Double	 		f_quantity;
	protected WPosTextField	f_name1;
	private Button			f_bBPartner;
	private Button			f_bSearch;
	private int orderLineId = 0;
	private int currentLayout;
	/** The Table					*/
	private WListbox		m_table;
	/** The Query SQL				*/
	private String			m_sql;
	/** Status Panel */
	private boolean status;
	private Panel all_SubCard;
	private Panel popular_SubCard;
	/**	Table Column Layout Info			*/
	private static ColumnInfo[] s_layout = new ColumnInfo[] 
	{
		new ColumnInfo(" ", "C_OrderLine_ID", IDColumn.class), 
		new ColumnInfo(Msg.translate(Env.getCtx(), "Name"), "p_Name", String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "Qty"), "QtyOrdered", Double.class,false,true,null),
		new ColumnInfo(Msg.translate(Env.getCtx(), "C_UOM_ID"), "UOM_name", String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "PriceActual"), "PriceActual", BigDecimal.class,false,true,null), 
		new ColumnInfo(Msg.translate(Env.getCtx(), "LineNetAmt"), "LineNetAmt", BigDecimal.class), 
		new ColumnInfo(Msg.translate(Env.getCtx(), "C_Tax_ID"), "TaxIndicator", String.class, true, true, null), 
		new ColumnInfo(Msg.translate(Env.getCtx(), "GrandTotal"), "GrandTotal", BigDecimal.class,  true, true, null), 
	};
	/**	From Clause							*/
	private static String s_sqlFrom ;
	/** Where Clause						*/
	private static String s_sqlWhere; 
	/** Map of map of keys */
	private HashMap<Integer, HashMap<Integer, MPOSKey>> keymap;
	private Panel button;

	private int keyLayoutId;
	PosOrderModel m_order = null;
	
	
	

	/**	The Product					*/
	private MProduct		m_product = null;

	/**	Price List Version to use	*/
	private int			m_M_PriceList_Version_ID = 0;
	/** Warehouse					*/
	private int 			m_M_Warehouse_ID;
	private ArrayList<Integer> orderList;
	private int recordposition;
	private int cont; 
	private final String POS_ALTERNATIVE_DOCTYPE_ENABLED = "POS_ALTERNATIVE_DOCTYPE_ENABLED";  // System configurator entry
	private final String NO_ALTERNATIVE_POS_DOCTYPE      = "N";
	private final boolean isAlternativeDocTypeEnabled    = MSysConfig.getValue(POS_ALTERNATIVE_DOCTYPE_ENABLED, 
			NO_ALTERNATIVE_POS_DOCTYPE, Env.getAD_Client_ID(p_ctx)).compareToIgnoreCase(NO_ALTERNATIVE_POS_DOCTYPE)==0?false:true;
	
	private final String BG_GRADIENT = "";
	private final String ACTION_BPARTNER    = "BPartner";
	private final String ACTION_CANCEL      = "Cancel";
	private final String ACTION_CREDITSALE  = "Credit Sale";
	private final String ACTION_HISTORY     = "History";
	private final String ACTION_NEW         = "New";
	private final String ACTION_PAYMENT     = "Payment";

	/**
	 * 	Initialize
	 */
	public void init()
	{
		//	Content
		this.setHeight("100%");
		this.setWidth("99%");
		status = false;
		cont  = 0;
		keymap = new HashMap<Integer, HashMap<Integer,MPOSKey>>();
		listOrder();
		recordposition = orderList.size()-1;
		
		s_sqlFrom = "POS_OrderLine_v";
		/** Where Clause						*/
		s_sqlWhere = "C_Order_ID=? AND LineNetAmt <> 0";
		
		Panel parameterPanel = new Panel();
		Borderlayout detailPanel = new Borderlayout();
		Grid parameterLayout = GridFactory.newGridLayout();
		Panel productPanel = new Panel();
		Borderlayout fullPanel = new Borderlayout();
		Grid productLayout = GridFactory.newGridLayout();
		Grid parameterLayout3 = GridFactory.newGridLayout();
		Rows rows = null;
		Row row = null;

		East east = new East();
		east.setStyle("border: none; width:40%");
		east.setAutoscroll(true);
		appendChild(east);
		productPanel.appendChild(productLayout);
		productLayout.setWidth("100%");
		rows = productLayout.newRows();
		row = rows.newRow();
		int C_POSKeyLayout_ID = p_pos.getC_POSKeyLayout_ID();
		if (C_POSKeyLayout_ID == 0)
			return;
		currentLayout = C_POSKeyLayout_ID;
		east.appendChild(
				createPanel(C_POSKeyLayout_ID));
		
		West west = new West();
		west.setStyle("border: none;");
		appendChild(west);
		west.appendChild(fullPanel);
		fullPanel.setWidth("100%");
		fullPanel.setHeight("100%");
		North north = new North();
		north.setStyle("border: none; width:60%");
		north.setZindex(0);
		fullPanel.appendChild(north);
		parameterPanel.appendChild(parameterLayout);
		parameterLayout.setWidth("60%");
		north.appendChild(parameterPanel);
		rows = parameterLayout.newRows();
		row = rows.newRow();
		
		setStyle("border: none");
		
		m_table = ListboxFactory.newDataTable();
		m_sql = m_table.prepareTable(s_layout, s_sqlFrom, 
			s_sqlWhere, false, "POS_OrderLine_v");
		m_table.autoSize();

		m_table.getModel().addTableModelListener(this);
		
		Center center = new Center();
		center.setStyle("border: none; width:400px");
		appendChild(center);
		center.appendChild(detailPanel);
		north = new North();
		north.setStyle("border: none");
		detailPanel.setHeight("40%");
		detailPanel.setWidth("50%");
		detailPanel.appendChild(north);
		
		keyLayoutId=p_pos.getOSNP_KeyLayout_ID();
		setQty(Env.ONE);
		
		setPrice(Env.ZERO);

		center = new Center();
		detailPanel.appendChild(center);
		center.appendChild(m_table);
		m_table.setWidth("100%");
		m_table.setHeight("99%");
		m_table.addActionListener(this);
		center.setStyle("border: none");
		m_table.loadTable(new PO[0]);
		
		north.appendChild(parameterLayout3);
		parameterLayout3.setWidth("100%");
		parameterLayout3.setHeight("100%");
		rows = parameterLayout3.newRows();
		parameterLayout3.setStyle("border:none");
		row = rows.newRow();
		row.setHeight("60px");

		row.appendChild(new Space());
		// NEW
		f_bNew = createButtonAction(ACTION_NEW, KeyStroke.getKeyStroke(KeyEvent.VK_F2, Event.F2));
		f_bNew.addActionListener(this);
		row.appendChild(f_bNew);

		// BPartner Search
		f_bBPartner = createButtonAction(ACTION_BPARTNER, p_pos.getOSK_KeyLayout_ID());
		f_bBPartner.addActionListener(this);
		row.appendChild(f_bBPartner);
				
		// EDIT
		f_bEdit = createButtonAction(ACTION_CREDITSALE, null);
		f_bEdit.addActionListener(this);
		row.appendChild(f_bEdit);
		f_bEdit.setEnabled(false);
				
		// HISTORY
		f_history = createButtonAction(ACTION_HISTORY, null);
		f_history.addActionListener(this);
		row.appendChild(f_history); 

		f_Back = createButtonAction("Parent", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
		row.appendChild (f_Back);
		f_Next = createButtonAction("Detail", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
		row.appendChild (f_Next);
		
		f_Up = createButtonAction("Previous", KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
		row.appendChild (f_Up);
		f_Down = createButtonAction("Next", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
		row.appendChild (f_Down);
		
		// PAYMENT
		f_cashPayment = createButtonAction(ACTION_PAYMENT, null);
		f_cashPayment.addActionListener(this);
		row.appendChild(f_cashPayment); 
		f_cashPayment.setEnabled(false);
		
		// LOGOUT
		f_logout = createButtonAction (ACTION_CANCEL, null);
		f_logout.addActionListener(this);
		row.appendChild (f_logout);
		row.appendChild(new Space());
		
		row = rows.newRow();
		row.setSpans("3,4,2");
		row.setHeight("30px");
		// BP
		Label bpartner = new Label(Msg.translate(Env.getCtx(), "C_BPartner_ID")+":");
		row.appendChild (bpartner.rightAlign());
		bpartner.setStyle("Font-size:medium; font-weight:700");
		
		f_name = new WPosTextField(p_posPanel, p_pos.getOSK_KeyLayout_ID());
		f_name.setStyle("Font-size:medium");
		f_name.setWidth("100%");
		f_name.setHeight("35px");
		f_name.addEventListener("onFocus",this);
		row.appendChild  (f_name);
		

		Label lNet = new Label (Msg.translate(Env.getCtx(), "SubTotal")+":");
		lNet.setStyle("Font-size:medium; font-weight:700");
		row.appendChild(lNet.rightAlign());
		f_net = new Label(String.valueOf(DisplayType.Amount));
		f_net.setStyle("Font-size:medium");
		row.appendChild(f_net);
		f_net.setText(Env.ZERO+"");
		
		//
		row = rows.newRow();
		row.setHeight("30px");
		row.setSpans("3,4,2");
		// DOC NO
		Label docNo = new Label(Msg.getMsg(Env.getCtx(),"DocumentNo")+":");
		row.appendChild (docNo.rightAlign());

		docNo.setStyle("Font-size:medium; font-weight:700");
		f_DocumentNo = new Label();
		f_DocumentNo.setStyle("Font-size:medium");
		row.appendChild(f_DocumentNo);
		
		Label lTax = new Label (Msg.translate(Env.getCtx(), "TaxAmt")+":");
		lTax.setStyle("Font-size:medium; font-weight:700");
		row.appendChild(lTax.rightAlign());
		f_tax = new Label(String.valueOf(DisplayType.Amount));
		f_tax.setStyle("Font-size:medium");
		row.appendChild(f_tax);
		f_tax.setText(Env.ZERO.toString());
		
		row = rows.newRow();
		row.setSpans("3,4,2");
		row.setHeight("30px");
		// SALES REP
		Label l_SalesRep = new Label(Msg.translate(Env.getCtx(), "SalesRep_ID")+":");
		row.appendChild(l_SalesRep.rightAlign());
		l_SalesRep.setStyle("Font-size:medium; font-weight:700");
		MUser salesRep = new MUser(p_ctx, Env.getAD_User_ID(p_ctx), null);
		f_RepName = new Label(salesRep.getName());
		f_RepName.setStyle("Font-size:medium");
		row.appendChild (f_RepName);
		
		Label lTotal = new Label (Msg.translate(Env.getCtx(), "GrandTotal")+":");
		lTotal.setStyle("Font-size:medium; font-weight:700");
		row.appendChild(lTotal.rightAlign());
		f_total = new Label(String.valueOf(DisplayType.Amount));
		row.appendChild(f_total);
		f_total.setText(Env.ZERO.toString());
		f_total.setStyle("Font-size:medium");
		
		row = rows.newRow();
		row.setSpans("3,3,2,2");
		row.setHeight("30px");
		row.appendChild(new Space());
		row.appendChild(new Space());
		//

	}	//	init
	
	public Panel createButton(int C_POSKeyLayout_ID){
		if ( keymap.containsKey(C_POSKeyLayout_ID) ) {
			return null;
		}
		Panel card = new Panel();
		card.setWidth("100%");
		MPOSKeyLayout keyLayout = MPOSKeyLayout.get(Env.getCtx(), C_POSKeyLayout_ID);
		Color stdColor = Color.lightGray;
		if (keyLayout.getAD_PrintColor_ID() != 0)
		{
			MPrintColor color = MPrintColor.get(Env.getCtx(), keyLayout.getAD_PrintColor_ID());
			stdColor = color.getColor();
		}
		if (keyLayout.get_ID() == 0)
			return null;
		MPOSKey[] keys = keyLayout.getKeys(false);
		
		HashMap<Integer, MPOSKey> map = new HashMap<Integer, MPOSKey>(keys.length);

		keymap.put(C_POSKeyLayout_ID, map);
		
		int COLUMNS = 3;	//	Min Columns
		int ROWS = 3;		//	Min Rows
		int noKeys = keys.length;
		int cols = keyLayout.getColumns();
		if ( cols == 0 )
			cols = COLUMNS;
		int buttons = 0;
		log.fine( "PosSubFunctionKeys.init - NoKeys=" + noKeys 
			+ ", Cols=" + cols);
		//	Content
		Panel content = new Panel ();

		Label productLabel = new Label(Msg.translate(Env.getCtx(), "M_Product_ID")+":");
		productLabel.setStyle("Font-size:medium; font-weight:700");
		content.appendChild(productLabel);
		
		f_name1 = new WPosTextField(p_posPanel, p_pos.getOSK_KeyLayout_ID());
		f_name1.setWidth("80%");
		f_name1.setHeight("35px");
		f_name1.setName("Name");
		f_name1.setReadonly(true);
		f_name1.addEventListener("onFocus", this);
		
		content.appendChild(f_name1);
		
		for (MPOSKey key :  keys)
		{
			if(!key.getName().equals("")){
			map.put(key.getC_POSKey_ID(), key);
			Color keyColor = stdColor;
			
			if (key.getAD_PrintColor_ID() != 0)	{
				MPrintColor color = MPrintColor.get(Env.getCtx(), key.getAD_PrintColor_ID());
				keyColor = color.getColor();
			}
			
			
			log.fine( "#" + map.size() + " - " + keyColor); 
			button = new Panel();
			Label label = new Label(key.getName());
			
			North nt = new North();
			South st = new South();
			Borderlayout mainLayout = new Borderlayout();
			if ( key.getAD_Image_ID() != 0 )
			{
				MImage m_mImage = MImage.get(Env.getCtx(), key.getAD_Image_ID());
				AImage img = null;
				byte[] data = m_mImage.getData();
				if (data != null && data.length > 0) {
					try {
						img = new AImage(null, data);				
					} catch (Exception e) {		
					}
				}
				Image bImg = new Image();
				bImg.setContent(img);
				bImg.setWidth("50%");
				bImg.setHeight("50px");
				nt.appendChild(bImg);
			}
			label.setStyle("word-wrap: break-word; white-space: pre-line;margin: 25px 0px 0px 0px; top:20px; font-size:10pt; font-weight: bold;color: #FFF;");
			label.setHeight("100%");
			button.setHeight("80px");
			st.appendChild(label);
			button.setClass("z-button");
			button.setStyle("float:left; white-space: pre-line;text-align:center; margin:1% 1%; Background-color:rgb("+keyColor.getRed()+","+keyColor.getGreen()+","+keyColor.getBlue()+"); border: 2px outset #CCC; "
					+ "background: -moz-linear-gradient(top, rgba(247,247,247,1) 0%, rgba(255,255,255,0.93) 7%, rgba(186,186,186,0.25) 15%, rgba("+keyColor.getRed()+","+keyColor.getGreen()+","+keyColor.getBlue()+",1) 100%);"
					+ "background: -webkit-gradient(left top, left bottom, color-stop(0%, rgba(247,247,247,1)), color-stop(7%, rgba(255,255,255,0.93)), color-stop(15%, rgba(186,186,186,0.25)), color-stop(100%, rgba("+keyColor.getRed()+","+keyColor.getGreen()+","+keyColor.getBlue()+",1)));"
					+ "background: -webkit-linear-gradient(top, rgba(247,247,247,1) 0%, rgba(255,255,255,0.93) 7%, rgba(186,186,186,0.25) 15%, rgba("+keyColor.getRed()+","+keyColor.getGreen()+","+keyColor.getBlue()+",1) 100%);");
			
			mainLayout.appendChild(nt);
			mainLayout.appendChild(st);
			mainLayout.setStyle("background-color: transparent");
			nt.setStyle("background-color: transparent");
			st.setStyle("clear: both; background-color: #333; opacity: 0.6;");
			st.setZindex(99);
			button.appendChild(mainLayout);
			
			button.setId(""+key.getC_POSKey_ID());
			button.addEventListener("onClick", this);

			int size = 1;
			if ( key.getSpanX() > 1 )
			{
				size = key.getSpanX();
				button.setWidth("96%");
			}
			else 
				button.setWidth(88/cols+"%");
			if ( key.getSpanY() > 1 )
			{
				size = size*key.getSpanY();
			}
			buttons = buttons + size;
			content.appendChild(button);
		}
		}
		int rows = Math.max ((buttons / cols), ROWS);
		if ( buttons % cols > 0 )
			rows = rows + 1;


		
		card.appendChild(content);
		
		return card;
	}
	public Panel createPanel(int C_POSKeyLayout_ID){
		Panel card = new Panel();
		card.setWidth("100%");
		MPOSKeyLayout keyLayout = MPOSKeyLayout.get(Env.getCtx(), C_POSKeyLayout_ID);
		
		if(popular_SubCard==null) {
			popular_SubCard = createButton(C_POSKeyLayout_ID);
			card.appendChild(popular_SubCard);
		}
		if (keyLayout.get_ID() == 0)
			return null;
		MPOSKey[] keys = keyLayout.getKeys(false);
		
		//	Content
		for (MPOSKey key :  keys)
		{
			if ( key.getSubKeyLayout_ID() > 0 )
			{
				if(all_SubCard == null){
					all_SubCard = createButton(key.getSubKeyLayout_ID());
				}
				if ( all_SubCard != null  ){
					if(status==false) {
						card.appendChild(all_SubCard);
						all_SubCard.setVisible(status);
						all_SubCard.setContext(""+key.getC_POSKey_ID());
						status=true;
					}
				}
					card.appendChild(all_SubCard);
			}
		}
		return card;
	}
	/**
	 * 	Dispose - Free Resources
	 */
	public void dispose()
	{
		f_name = null;
		super.dispose();
	}	//	dispose

	/**
	 * 
	 */
	private void printOrder() {
		{
			if (isOrderFullyPaid())
			{
				updateOrder();
				printTicket();
//				openCashDrawer();
			}
		}
	}

	/**
	 * 
	 */
	private void payOrder() {

		//Check if order is completed, if so, print and open drawer, create an empty order and set cashGiven to zero
		if( m_order == null ) {
				FDialog.warn(0, Msg.getMsg(p_ctx, "You must create an Order first"));
				return;
		}
		else if ( WPosPayment.pay(p_posPanel, this) ) {
				printTicket();
				setOrder(0);
		}
	}
	
	/**
	 * Execute order prepayment
	 * If order is not processed, process it first.
	 * If it is successful, proceed to pay and print ticket
	 */
	private void prePayOrder() {
		//Check if order is completed, if so, print and open drawer, create an empty order and set cashGiven to zero
		if( m_order == null) {		
			FDialog.warn(0, Msg.getMsg(p_ctx, "You must create an Order first"));
		}
		else
		{
			if ( WPosPrePayment.pay(p_posPanel, this) )
			{
				p_posPanel.setOrder(0);
			}
		}	
	}  // prePayOrder
	
	/**
	 * @param m_c_order_id
	 */
	public void setOrder(int m_c_order_id) 
	{
		if ( m_c_order_id == 0 )
			m_order = null;
		else
			m_order = new PosOrderModel(p_ctx , m_c_order_id, null, p_pos);
	}
	
	/**
	 * 
	 */
	private void deleteOrder() {
		if (m_order == null){
			FDialog.warn(0, Msg.getMsg(p_ctx, "You must create an Order first"));
			return;			
		}
		else if ( m_order.getDocStatus().equals(MOrder.STATUS_Drafted) ) {
			if (FDialog.ask(0, this, Msg.getMsg(p_ctx, "Do you want to delete the Order?"))) {
				if (m_order.deleteOrder())
					m_order = null;	
				else
					FDialog.warn(0,  Msg.getMsg(p_ctx, "Order could not be deleted"));
			}
		}
		else if (m_order.getDocStatus().equals(MOrder.STATUS_Completed)) {	
			if (FDialog.ask(0, this, Msg.getMsg(p_ctx, Msg.getMsg(p_ctx, "The order is already completed. Do you want to void it?")))) {		
				if (! m_order.cancelOrder())
					FDialog.warn(0,  Msg.getMsg(p_ctx, "Order could not be voided"));
			}
		}
		else {
			FDialog.warn(0,  Msg.getMsg(p_ctx, "Order is not Drafted nor Completed. Try to delete it other way"));
			return;
		}
	}
	
	/**
	 * 	Find/Set BPartner
	 */
	private void findBPartner()
	{
		
		String query = f_name.getValue();
		
		if (query == null || query.length() == 0)
			return;
		
		// unchanged
		if ( m_bpartner != null && m_bpartner.getName().equals(query))
			return;
		
		query = query.toUpperCase();
		//	Test Number
		boolean allNumber = true;
		boolean noNumber = true;
		char[] qq = query.toCharArray();
		for (int i = 0; i < qq.length; i++)
		{
			if (Character.isDigit(qq[i]))
			{
				noNumber = false;
				break;
			}
		}
		try
		{
			Integer.parseInt(query);
		}
		catch (Exception e)
		{
			allNumber = false;
		}
		String Value = query;
		String Name = (allNumber ? null : query);
		String EMail = (query.indexOf('@') != -1 ? query : null); 
		String Phone = (noNumber ? null : query);
		String City = null;
		//
		//TODO: contact have been remove from rv_bpartner
		MBPartnerInfo[] results = MBPartnerInfo.find(p_ctx, Value, Name, 
			/*Contact, */null, EMail, Phone, City);
		
		//	Set Result
		if (results.length == 0)
		{
			setC_BPartner_ID(0);
		}
		else if (results.length == 1)
		{
			setC_BPartner_ID(results[0].getC_BPartner_ID());
			f_name.setText(results[0].getName());
		}
		else	//	more than one
		{
			WQueryBPartner qt = new WQueryBPartner(p_posPanel, this);
			qt.setResults (results);
			qt.setVisible(true);
		}
	}	//	findBPartner
	
	
	/**************************************************************************
	 * 	Set BPartner
	 *	@param C_BPartner_ID id
	 */
	public void setC_BPartner_ID (int C_BPartner_ID)
	{
		log.fine( "PosSubCustomer.setC_BPartner_ID=" + C_BPartner_ID);
		if (C_BPartner_ID == 0)
			m_bpartner = null;
		else
		{
			m_bpartner = new MBPartner(p_ctx, C_BPartner_ID, null);
			if (m_bpartner.get_ID() == 0)
				m_bpartner = null;
		}
		
		//	Set Info
		if (m_bpartner != null)
		{
			f_name.setText(m_bpartner.getName());
		}
		else
		{
			f_name.setText(null);
		}
		//	Sets Currency
		m_M_PriceList_Version_ID = 0;
		getM_PriceList_Version_ID();
		//fillCombos();
		if ( m_order != null && m_bpartner != null )
			m_order.setBPartner(m_bpartner);  //added by ConSerTi to update the client in the request
	}	//	setC_BPartner_ID

	/**
	 * 	Get BPartner
	 *	@return C_BPartner_ID
	 */
	public int getC_BPartner_ID ()
	{
		if (m_bpartner != null)
			return m_bpartner.getC_BPartner_ID();
		return 0;
	}	//	getC_BPartner_ID

	/**
	 * 	Get BPartner
	 *	@return BPartner
	 */
	public MBPartner getBPartner ()
	{
		return m_bpartner;
	}	//	getBPartner
	
	
	

	/**
	 * 	Get M_PriceList_Version_ID.
	 * 	Set Currency
	 *	@return plv
	 */
	public int getM_PriceList_Version_ID()
	{
		if (m_M_PriceList_Version_ID == 0)
		{
			int M_PriceList_ID = p_pos.getM_PriceList_ID();
			if (m_bpartner != null && m_bpartner.getM_PriceList_ID() != 0)
				M_PriceList_ID = m_bpartner.getM_PriceList_ID();
			//
			MPriceList pl = MPriceList.get(p_ctx, M_PriceList_ID, null);
			setCurrency(MCurrency.getISO_Code(p_ctx, pl.getC_Currency_ID()));

			//
			MPriceListVersion plv = pl.getPriceListVersion (p_posPanel.getToday());
			if (plv != null && plv.getM_PriceList_Version_ID() != 0)
				m_M_PriceList_Version_ID = plv.getM_PriceList_Version_ID();
		}
		return m_M_PriceList_Version_ID;
	}	//	getM_PriceList_Version_ID
	

	/***************************************************************************
	 * Set Currency
	 * 
	 * @param currency
	 *            currency
	 */
	public void setCurrency(String currency) {
		if (currency == null)
			f_currency.setText("---");
		else
			f_currency.setText(currency);
	} //	setCurrency
	
	/**
	 * 	Print Ticket
	 *  @author Raul Muñoz raulmunozn@gmail.com 
	 */
	public void printTicket()
	{
		if ( m_order == null )
			return;
		
		MOrder order = m_order;
		//int windowNo = p_posPanel.getWindowNo();
		//Properties m_ctx = p_posPanel.getPropiedades();
		
		if (order != null)
		{
			try 
			{
				//print standard document
				Boolean print = true;
				if (p_pos.getAD_Sequence_ID() != 0)
				{
					MSequence seq = new MSequence(Env.getCtx(), p_pos.getAD_Sequence_ID(), order.get_TrxName());
					String docno = seq.getPrefix() + seq.getCurrentNext();
					String q = "Confirmar el número consecutivo "  + docno;
					if (FDialog.ask(0, null, q))						
					{
						order.setPOReference(docno);
						order.saveEx();
						ReportCtl.startDocumentPrint(0, order.getC_Order_ID(), false);
						int next = seq.getCurrentNext() + seq.getIncrementNo();
						seq.setCurrentNext(next);
						seq.saveEx();
					}
				}
				else
					ReportCtl.startDocumentPrint(0, order.getC_Order_ID(), false);				
			}
			catch (Exception e) 
			{
				log.severe("PrintTicket - Error Printing Ticket");
			}
		}	  
	}
	
	/**
	 * Is order fully pay 
	 * @author Raul Muñoz 
	 */
	public boolean isOrderFullyPaid()
	{
		/*TODO
		BigDecimal given = new BigDecimal(f_cashGiven.getValue().toString());
		boolean paid = false;
		if (p_posPanel != null && p_posPanel.f_curLine != null)
		{
			MOrder order = p_posPanel.f_curLine.getOrder();
			BigDecimal total = new BigDecimal(0);
			if (order != null)
				total = order.getGrandTotal();
			paid = given.doubleValue() >= total.doubleValue();
		}
		return paid;
		*/
		return true;
	}
	
	/**
	 * 	Update Order
	 *  @author Raul Muñoz 
	 */
	public void updateOrder()
	{
		if (p_posPanel != null )
		{
			MOrder order = m_order;
			if (order != null)
			{
  				f_DocumentNo.setText(order.getDocumentNo());
  				setC_BPartner_ID(order.getC_BPartner_ID());
  				f_bNew.setEnabled(order.getLines().length != 0);
  				f_bEdit.setEnabled(true);
  				f_history.setEnabled(order.getLines().length != 0);
  				f_cashPayment.setEnabled(order.getLines().length != 0);
			}
			else
			{
				f_DocumentNo.setText("");
				setC_BPartner_ID(0);
				f_bNew.setEnabled(true);
				f_bEdit.setEnabled(false);
				f_history.setEnabled(true);
				f_cashPayment.setEnabled(false);
			}
			
		}
	}	

	/**
	 * 	Open Box
	 *  @author Raul Muñoz 
	 */
	public void openCashDrawer()
	{
		String port = "/dev/lp";
		
		byte data[] = new byte[] {0x1B, 0x40, 0x1C};
		try {  
            FileOutputStream m_out = null;
			if (m_out == null) {
                m_out = new FileOutputStream(port);  // No poner append = true.
            }
            m_out.write(data);
        } catch (IOException e) {
        }  
	}	

	/**
	 * 	Set Sums from Table
	 */
	void setSums(PosOrderModel order)
	{
		int noLines = m_table.getRowCount();
		if (order == null || noLines == 0)
		{
			f_net.setText(String.valueOf(Env.ZERO.doubleValue()));
			f_total.setValue(String.valueOf(Env.ZERO.doubleValue()));
			f_tax.setValue(String.valueOf(Env.ZERO.doubleValue()));
		}
		else
		{
			// order.getMOrder().prepareIt();
			f_net.setValue(order.getSubtotal().toString());
			f_total.setValue(order.getGrandTotal().toString());
			f_tax.setValue(order.getTaxAmt().toString());

		}
	}	//	setSums

	private void onCreditSale()
	{
		if( m_order != null ) 
		{

			if ( !m_order.isProcessed() && !m_order.processOrder() )
			{
				FDialog.warn(0, "PosOrderProcessFailed");
				return;
			}
		}
	}

	@Override
	public void tableChanged(WTableModelEvent event) {
		int row = m_table.getSelectedRow();
		if (row != -1 )
		{
			Object data = m_table.getModel().getValueAt(row, 0);
			if ( data != null )
			{
				Integer id = (Integer) ((IDColumn)data).getRecord_ID();
				orderLineId = id;
				loadLine(id);
			}
		}
		if (event.getModel().equals(m_table.getModel())) //Add Minitable Source Condition
			valueChange();
	}
	
	public void valueChange() {
		
		int id = m_table.getSelectedRow();
		ListModelTable model = m_table.getModel();
		if (id != -1) {	
		IDColumn key = (IDColumn) model.getValueAt(id, 0);
		
		if ( key != null &&  key.getRecord_ID() != orderLineId )
			orderLineId = key.getRecord_ID();
			MOrderLine line = new MOrderLine(p_ctx, orderLineId, null);
			if ( line != null )
			{
				
					line.setPrice(new BigDecimal(m_table.getModel().getValueAt(id, 4).toString()));
					line.setQty(new BigDecimal(m_table.getModel().getValueAt(id, 2).toString()));
					line.saveEx();
					updateInfo();
				}
			
		}

	}
	
	private void loadLine(int lineId) {
		
		if ( lineId <= 0 )
			return;
	
		log.fine("SubCurrentLine - loading line " + lineId);
		MOrderLine ol = new MOrderLine(p_ctx, lineId, null);
		if ( ol != null )
		{
			setPrice(ol.getPriceActual());
			setQty(ol.getQtyOrdered());
		}
		
	}
	
	@Override
	public void onEvent(org.zkoss.zk.ui.event.Event e) throws Exception {
		String action = e.getTarget().getId();
		if (e.getTarget().equals(f_bNew)) {
				newOrder(); 
				e.stopPropagation();
			}
		else if (e.getTarget().equals(f_bEdit))
			onCreditSale();
		else if(e.getTarget().equals(f_cashPayment)){
			payOrder();
		}
		else if (e.getTarget().equals(f_Back) ){
			previousRecord();
			updateInfo();
			return;
		}
		else if (e.getTarget().equals(f_Next) ){
			nextRecord();
			updateInfo();
			return;
		}
		else if (e.getTarget().equals(f_print))
			printOrder();
		else if(e.getTarget().equals(f_logout)){
			dispose();
			return;
		}
		else if (e.getTarget().equals(f_Up)){
			int rows = m_table.getRowCount();
			if (rows == 0)
				return;
			int row = m_table.getSelectedRow();
			row--;
			if (row < 0)
				row = 0;
			m_table.setSelectedIndex(row);
			return;
		}
		else if (e.getTarget().equals(f_Down)){
			int rows = m_table.getRowCount();
			if (rows == 0)
				return;
			int row = m_table.getSelectedRow();
			row++;
			if (row >= rows)
				row = rows - 1;
			m_table.setSelectedIndex(row);
			return;
		}
		else if (e.getTarget().equals(f_name1) ){
			cont++;
			if(cont<2){
				if(e.getName().equals("onFocus")) {
				WPOSKeyboard keyboard = p_posPanel.getKeyboard(f_name1.getKeyLayoutId()); 
				keyboard.setTitle(Msg.translate(Env.getCtx(), "M_Product_ID"));
				keyboard.setPosTextField(this.f_name1);	
				if(e.getName().equals("onFocus")) {
					keyboard.setVisible(true);
					keyboard.setWidth("750px");
					keyboard.setHeight("380px");
					AEnv.showWindow(keyboard);
					findProduct();
				}
				}
			}
			else {
				cont=0;
				f_bBPartner.setFocus(true);
			}
			updateInfo();
			return;
		}
			//  Partner
		else if (e.getTarget().equals(f_name)) {
			cont++;
			if(cont<2){
				if(e.getName().equals("onFocus")) {
					setParameter();
					WQueryBPartner qt = new WQueryBPartner(p_posPanel, this);
				
					qt.setVisible(true);
				
					AEnv.showWindow(qt);
					findBPartner();
					if(m_table.getRowCount() > 0){
						int row = m_table.getSelectedRow();
						if (row < 0) row = 0;
						m_table.setSelectedIndex(row);
					}
				}
			}
				else {
					cont=0;
					f_bNew.setFocus(true);
				}
				
		}
		//	Product
		else if (e.getTarget().equals(f_bSearch))
			{
				setParameter();
				WQueryProduct qt = new WQueryProduct(p_posPanel, this);
				
				qt.setQueryData(m_M_PriceList_Version_ID, m_M_Warehouse_ID);
				qt.setVisible(true);
				
				AEnv.showWindow(qt);
				findProduct();
				if(m_table.getRowCount() > 0){
					int row = m_table.getSelectedRow();
					if (row < 0) row = 0;
					m_table.setSelectedIndex(row);
				}
		}
		else if (e.getTarget().equals(f_process))
			deleteOrder();		

		//	Delete
		else if (e.getTarget().equals(f_delete))
		{
			int rows = m_table.getRowCount();
			if (rows != 0)
			{
				int row = m_table.getSelectedRow();
				if (row != -1)
				{
					if ( m_order != null )
						m_order.deleteLine(m_table.getSelectedRowKey());
					setQty(null);
					setPrice(null);
		
					orderLineId = 0;
				}
			}
			updateInfo();
			return;
		}
	
		//	Register
		if (e.getTarget().equals(f_history)) {
			
			WPosQuery qt = new WQueryTicket(p_posPanel, this);
			qt.setVisible(true);
			AEnv.showWindow(qt);
			updateInfo();
			return;
		}
	
		//	Discount
		else if (e.getTarget().equals(f_discount)) {
			cont++;
			if(cont<2){
				if(e.getName().equals("onFocus")) {
				setParameter();
				WPOSKeyboard keyboard = p_posPanel.getKeyboard(keyLayoutId); 
				keyboard.setWidth("280px");
				keyboard.setHeight("320px");
				keyboard.setPosTextField(this.f_discount);	
				AEnv.showWindow(keyboard);
				findProduct();
				if(m_table.getRowCount() > 0){
					int row = m_table.getSelectedRow();
					if (row < 0) row = 0;
					m_table.setSelectedIndex(row);
				}
				}
				MOrderLine line = new MOrderLine(p_ctx, orderLineId, null);
				if ( line != null )
				{
					line.setDiscount(new BigDecimal(f_discount.getValue().toString()));
					line.saveEx();
					updateInfo();
				}
			}
				else {
					cont=0;
					f_bBPartner.setFocus(true);
				}
			}
			
		if (action == null || action.length() == 0 || keymap == null)
			return;
		log.info( "PosSubFunctionKeys - actionPerformed: " + action);
		HashMap<Integer, MPOSKey> currentKeymap = keymap.get(currentLayout);
		
		try
		{
			int C_POSKey_ID = Integer.parseInt(action);
			MPOSKey key = currentKeymap.get(C_POSKey_ID);
			// switch layout
			if ( key.getSubKeyLayout_ID() > 0 )
			{
				currentLayout = key.getSubKeyLayout_ID();
				if(all_SubCard.getContext().equals(e.getTarget().getId())){
					all_SubCard.setVisible(true);
					popular_SubCard.setVisible(false);
				}
				else {
					all_SubCard.setVisible(false);
					popular_SubCard.setVisible(true);
				}
			}
			else
			{
				keyReturned(key);
			}
			return;
		}
		catch (Exception ex)
		{
		}
		if(m_table.equals(e.getTarget())){
			return;
		}
		updateInfo();
	}

	/**
	 * 	Find/Set Product & Price
	 */
	private void findProduct()
	{
		String query = f_name1.getText();
		if (query == null || query.length() == 0)
			return;
		query = query.toUpperCase();
		//	Test Number
		boolean allNumber = true;
		try
		{
			Integer.getInteger(query);
		}
		catch (Exception e)
		{
			allNumber = false;
		}
		String Value = query;
		String Name = query;
		String UPC = (allNumber ? query : null);
		String SKU = (allNumber ? query : null);
		
		MWarehousePrice[] results = null;
		setParameter();
		//
		results = MWarehousePrice.find (p_ctx,
			m_M_PriceList_Version_ID, m_M_Warehouse_ID,
			Value, Name, UPC, SKU, null);
		
		//	Set Result
		if (results.length == 0)
		{
			String message = Msg.translate(p_ctx,  "search.product.notfound");
			FDialog.warn(0, p_posPanel, message + query,"");
			setM_Product_ID(0);
			setPrice(Env.ZERO);
		}
		else if (results.length == 1)
		{
			setM_Product_ID(results[0].getM_Product_ID());
			setQty(Env.ONE);
			f_name.setText(results[0].getName());
			setPrice(results[0].getPriceStd());
			saveLine();
		}
		else	//	more than one
		{
			WQueryProduct qt = new WQueryProduct(p_posPanel, this);
			qt.setResults(results);
			qt.setQueryData(m_M_PriceList_Version_ID, m_M_Warehouse_ID);
			qt.setVisible(true);
		}
	}	//	findProduct
	
	/**
	 * Call back from key panel
	 */
	public void keyReturned(MPOSKey key) {
		// processed order
		if ( p_posPanel.m_order != null && p_posPanel.m_order.isProcessed() )
			return;
		
		// new line
		setM_Product_ID(key.getM_Product_ID());
		setPrice();
		setQty(key.getQty());
		if ( !saveLine() ) {
			FDialog.error(0, this, "Could not save order line");
		}
		updateInfo();
		return;
	}
	/**
	 * Save Line
	 * 
	 * @return true if saved
	 */
	public boolean saveLine() {
		MProduct product = getProduct();
		if (product == null)
			return false;
		BigDecimal QtyOrdered  = BigDecimal.valueOf(f_quantity);
		BigDecimal PriceActual = BigDecimal.valueOf(f_price);
		if (m_order == null ) {
			m_order = PosOrderModel.createOrder(p_pos, getBPartner());
		}
		
		MOrderLine line = null;
		
		if ( m_order != null ) {
			line = m_order.createLine(product, QtyOrdered, PriceActual);
			
			if (line == null)
				return false;
			line.saveEx();
		}
		
		orderLineId = line.getC_OrderLine_ID();
		setM_Product_ID(0);
		//
		return true;
	} //	saveLine
	

	/**
	 * 	Set Query Parameter
	 */
	private void setParameter()
	{
		//	What PriceList ?
		m_M_Warehouse_ID = p_pos.getM_Warehouse_ID();
		m_M_PriceList_Version_ID = getM_PriceList_Version_ID();
	}	//	setParameter
	
	/**
	 * 	Get Product
	 *	@return product
	 */
	public MProduct getProduct()
	{
		return m_product;
	}	//	getProduct
	
	/**
	 * 	Set Price for defined product 
	 */
	public void setPrice()
	{
		if (m_product == null)
			return;
		//
		setParameter();
		MWarehousePrice result = MWarehousePrice.get (m_product,
			m_M_PriceList_Version_ID, m_M_Warehouse_ID, null);
		if (result != null)
			setPrice(result.getPriceStd());
		else
			setPrice(Env.ZERO);
	}	//	setPrice
	
	/**
	 * 	New Order
	 *   
	 */
	public void newOrder()
	{
		log.info( "PosPanel.newOrder");
		setC_BPartner_ID(0);
		m_order = null;
		m_order = PosOrderModel.createOrder(p_pos, getBPartner());
		if (FDialog.ask(0, null, "¿Quiere generar un crédito fiscal?"))	{
			m_order.setC_DocTypeTarget_ID(p_pos.getC_DocTypewholesale_ID());
		}

		newLine();
		
		updateInfo();
	}	//	newOrder

	/**
	 * 	Update Table
	 *	@param order order
	 */
	public void updateTable (PosOrderModel order)
	{
		int C_Order_ID = 0;
		if (order != null)
			C_Order_ID = order.getC_Order_ID();
		if (C_Order_ID == 0)
		{
			m_table.loadTable(new PO[0]);
			setSums(null);
		}
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement (m_sql, null);
			pstmt.setInt (1, C_Order_ID);
			rs = pstmt.executeQuery();
			m_table.loadTable(rs);
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, m_sql, e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		
		for ( int i = 0; i < m_table.getRowCount(); i ++ )
		{
			IDColumn key = (IDColumn) m_table.getModel().getValueAt(i, 0);
			if ( key != null && orderLineId > 0 && key.getRecord_ID() == orderLineId )
			{
				// 31-07-2015
				m_table.setSelectedIndex(i);
				break;
			}
		}

		setSums(order);
		
	}	//	updateTable
	

	public void updateInfo()
	{
		// reload order
		if ( m_order != null )
		{
			m_order.reload();
			updateTable(m_order);
			updateOrder();
		}
		
	}

	/**
	 * New Line
	 */
	public void newLine() {
		setM_Product_ID(0);
		setQty(Env.ONE);
		setPrice(Env.ZERO);
		orderLineId = 0;
	} //	newLine
	
	public void setPrice(BigDecimal price) {
		if (price == null)
			price = Env.ZERO;
		f_price=price.doubleValue();
	} //	setPrice
	public void setQty(BigDecimal qty) {
		if (qty == null)
			qty = Env.ZERO;
		f_quantity=qty.doubleValue();
	} //
	
	/**
	 * 	Set Product
	 *	@param M_Product_ID id
	 */
	public void setM_Product_ID (int M_Product_ID) {
		log.fine( "PosSubProduct.setM_Product_ID=" + M_Product_ID);
		if (M_Product_ID <= 0)
			m_product = null;
		else
		{
			m_product = MProduct.get(p_ctx, M_Product_ID);
			if (m_product.get_ID() == 0)
				m_product = null;
		}
		//	Set String Info
		if (m_product != null)
		{
			f_name1.setText(m_product.getName());
		}
		else
		{
			f_name1.setText(null);
		}
		
	}	//	setM_Product_ID

	@Override
	public void tableValueChange(TableValueChangeEvent event) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Previous Record Order
	 */
	public void previousRecord() {
		if(recordposition>0)
			setOrder(orderList.get(recordposition--));
	}

	/**
	 * Next Record Order
	 */
	public void nextRecord() {
		if(recordposition < orderList.size()-1)
			setOrder(orderList.get(recordposition++));
		
	}
	
	/**
	 * Get Data List Order
	 */
	public void listOrder() {
		String sql = "";
		PreparedStatement pstm;
		ResultSet rs;
		orderList = new ArrayList<Integer>();
		try 
		{
			sql=" SELECT o.C_Order_ID"
					+ " FROM C_Order o"
					+ " LEFT JOIN c_invoice i on i.c_order_ID = o.c_order_ID"
					+ " WHERE"
					+ " coalesce(invoiceopen(i.c_invoice_ID, 0), 0)  >= 0"
					+ " ORDER BY o.dateordered Asc";
			
			pstm= DB.prepareStatement(sql, null);
			rs = pstm.executeQuery();
			int i = 0;
			while(rs.next()){
				orderList.add(rs.getInt(1));
				
			}
		}
		catch(Exception e)
		{
			log.severe("QueryTicket.setResults: " + e + " -> " + sql);
		}
	}
	
}