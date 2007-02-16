//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package net.sf.gogui.gui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import net.sf.gogui.gtp.GtpError;
import net.sf.gogui.util.StringUtil;

/** Dialog for editing parameters in response to an analyze command of type
    <i>param</i>.
*/
public class ParameterDialog
{
    public static void editParameters(String paramCommand, Frame owner,
                                      String title, String response,
                                      GuiGtpClient gtp,
                                      MessageDialogs messageDialogs)
    {
        ArrayList parameters = parseResponse(response);
        int numberParameters = parameters.size();
        Box outerBox = Box.createHorizontalBox();
        int i = 0;
        int numberColumns = 0;
        Box box = null;
        while (i < numberParameters)
        {
            if (i % 30 == 0)
            {
                if (box != null)
                {
                    if (numberColumns > 0)
                    {
                        outerBox.add(GuiUtil.createFiller());
                        outerBox.add(new JSeparator(SwingConstants.VERTICAL));
                        outerBox.add(GuiUtil.createFiller());
                    }
                    outerBox.add(box);
                    ++numberColumns;
                }
                box = Box.createVerticalBox();
            }
            box.add(((Parameter)parameters.get(i)).getComponent());
            ++i;
        }
        if (box != null)
        {
            if (numberColumns > 0)
            {
                outerBox.add(GuiUtil.createFiller());
                outerBox.add(new JSeparator(SwingConstants.VERTICAL));
                outerBox.add(GuiUtil.createFiller());
            }
            outerBox.add(box);
        }
        Object options[] = { "Ok", "Cancel" };
        int r =
            JOptionPane.showOptionDialog(owner, outerBox, title,
                                         JOptionPane.OK_CANCEL_OPTION,
                                         JOptionPane.PLAIN_MESSAGE, null,
                                         options, options[0]);
        if (r != 0)
            return;
        for (i = 0; i < parameters.size(); ++i)
        {
            Parameter parameter = (Parameter)parameters.get(i);
            if (! parameter.isChanged())
                continue;
            String key = parameter.getKey();
            String newValue = parameter.getNewValue();
            String command = paramCommand + " " + key + " " + newValue;
            try
            {
                gtp.send(command);
            }
            catch (GtpError e)
            {
                messageDialogs.showError(owner,
                                        "Could not change parameter "
                                        + parameter.getKey(),
                                        StringUtil.capitalize(e.getMessage())
                                        );
            }
        }
    }

    private abstract static class Parameter
    {
        public Parameter(String key, String value)
        {
            m_key = key;
            m_value = value;
            m_label = StringUtil.capitalize(key.replace('_', ' '));
        }

        public String getKey()
        {
            return m_key;
        }

        public String getLabel()
        {
            return m_label;
        }

        public String getValue()
        {
            return m_value;
        }

        public abstract String getNewValue();

        public abstract boolean isChanged();

        public abstract Component getComponent();

        private final String m_key;

        private final String m_label;

        private final String m_value;
    }
    
    private static class BoolParameter
        extends Parameter
    {
        public BoolParameter(String key, String value)
        {
            super(key, value);
            try
            {
                m_bool = (Integer.parseInt(value) != 0);
            }
            catch (NumberFormatException e)
            {
                m_bool = false;
            }
            m_panel = new JPanel(new FlowLayout(FlowLayout.LEFT,
                                                GuiUtil.SMALL_PAD, 0));
            m_checkBox = new JCheckBox(getLabel(), m_bool);
            m_panel.add(m_checkBox);
        }

        public String getNewValue()
        {
            if (m_checkBox.isSelected())
                return "1";
            return "0";
        }

        public boolean isChanged()
        {
            return m_checkBox.isSelected() != m_bool;
        }

        public Component getComponent()
        {
            return m_panel;
        }

        private boolean m_bool;

        private final JCheckBox m_checkBox;

        private final JPanel m_panel;
    }

    private static class StringParameter
        extends Parameter
    {
        public StringParameter(String key, String value)
        {
            super(key, value);
            m_panel = new JPanel(new FlowLayout(FlowLayout.RIGHT,
                                                GuiUtil.SMALL_PAD,
                                                GuiUtil.SMALL_PAD));
            m_panel.add(new JLabel(getLabel() + ":"));
            m_textField = new JTextField(13);
            m_textField.setText(value);
            m_panel.add(m_textField);
        }

        public String getNewValue()
        {
            return m_textField.getText().trim();
        }

        public boolean isChanged()
        {
            return ! getNewValue().equals(getValue());
        }

        public Component getComponent()
        {
            return m_panel;
        }

        private final JTextField m_textField;

        private final JPanel m_panel;
    }

    private static ArrayList parseResponse(String response)
    {
        ArrayList parameters = new ArrayList();
        BufferedReader reader =
            new BufferedReader(new StringReader(response));
        while (true)
        {
            String line = null;
            try
            {
                line = reader.readLine();
            }
            catch (IOException e)
            {
            }
            if (line == null)
                break;
            line = line.trim();
            final int string = 0;
            final int bool = 1;
            int type = string;
            if (line.startsWith("[") && line.endsWith("]"))
            {
                // Might be used as label for grouping parameters on tabbing
                // panes in a later version of GoGui, so we silently accept it
                continue;
            }
            if (line.startsWith("[bool]"))
            {
                type = bool;
                line = line.replaceFirst("\\[bool\\]", "").trim();
            }
            else if (line.startsWith("[string]"))
            {
                type = string;
                line = line.replaceFirst("\\[string\\]", "").trim();
            }
            else if (line.startsWith("["))
            {
                // Treat unknown types as string for compatibiliy with future
                // types
                type = string;
                int pos = line.indexOf(']');
                if (pos >= 0)
                    line = line.substring(pos + 1).trim();                
            }
            int pos = line.indexOf(' ');
            String key;
            String value;
            if (pos < 0)
            {
                key = line.trim();
                value = "";
            }
            else
            {
                key = line.substring(0, pos).trim();
                value = line.substring(pos + 1).trim();
            }
            if (type == bool)
                parameters.add(new BoolParameter(key, value));
            else
                parameters.add(new StringParameter(key, value));
        }
        return parameters;
    }
}

